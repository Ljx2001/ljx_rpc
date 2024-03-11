package com.ljx.ChannelHandler.handler;

import ch.qos.logback.classic.net.SocketReceiver;
import com.ljx.Exceptions.ResponseException;
import com.ljx.RpcBootstrap;
import com.ljx.enumeration.ResponseCode;
import com.ljx.loadbalancer.LoadBalancer;
import com.ljx.protection.CircuitBreaker;
import com.ljx.transport.message.RpcRequest;
import com.ljx.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 这是一个测试的类
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress,CircuitBreaker> everyIpCircuitBreaker = RpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
        byte code = rpcResponse.getCode();
        System.out.println("code:"+code);
        if(code== ResponseCode.FAIL.getCode()){
            circuitBreaker.record(false);
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，返回错误的结果，响应码【{}】",rpcResponse.getRequestId(),code);
            throw new ResponseException(code,ResponseCode.FAIL.getMessage());
        }else if(code== ResponseCode.RESOURCE_NOT_FOUND.getCode()){
            circuitBreaker.record(false);
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，未找到目标资源，响应码【{}】",rpcResponse.getRequestId(),code);
            throw new ResponseException(code,ResponseCode.RESOURCE_NOT_FOUND.getMessage());
        }else if(code== ResponseCode.RATE_LIMIT.getCode()){
            circuitBreaker.record(false);
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，被限流，响应码【{}】",rpcResponse.getRequestId(),code);
            throw new ResponseException(code,ResponseCode.RATE_LIMIT.getMessage());
        } else if(code== ResponseCode.SUCCESS_HEARTBEAT.getCode()){
            completableFuture.complete(null);
            if(log.isDebugEnabled()){
                log.debug("心跳检测响应成功。");
            }
        } else if(code== ResponseCode.BECLOSEING.getCode()){
            completableFuture.complete(null);
            if(log.isDebugEnabled()){
                log.debug("id为【{}】的请求，访问被拒绝，目标服务器正处于关闭中。",rpcResponse.getRequestId());
            }
            RpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            LoadBalancer loadBalancer = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(rpcRequest.getRequestPayload().getInterfaceName(),RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
        }else if(code== ResponseCode.SUCCESS.getCode()){
            circuitBreaker.record(true);
            Object returnValue = rpcResponse.getBody();
            completableFuture.complete(returnValue);
            if(log.isDebugEnabled()){
                log.debug("已寻找到编号为【{}】的completableFuture，处理相应结果。",rpcResponse.getRequestId());
            }
        }
    }
}
