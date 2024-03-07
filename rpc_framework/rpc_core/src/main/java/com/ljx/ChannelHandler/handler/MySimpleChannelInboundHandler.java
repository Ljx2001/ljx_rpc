package com.ljx.ChannelHandler.handler;

import com.ljx.RpcBootstrap;
import com.ljx.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
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
        Object returnValue = rpcResponse.getBody();
        returnValue = returnValue==null?new Object():returnValue;
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
        completableFuture.complete(returnValue);
        if(log.isDebugEnabled()){
            log.debug("已寻找到编号为【{}】的completableFuture，处理相应结果。",rpcResponse.getRequestId());
        }
    }
}
