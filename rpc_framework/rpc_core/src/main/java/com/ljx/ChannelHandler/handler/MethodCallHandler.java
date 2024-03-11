package com.ljx.ChannelHandler.handler;

import com.ljx.RpcBootstrap;
import com.ljx.ServiceConfig;
import com.ljx.core.ShutDownHolder;
import com.ljx.enumeration.RequestType;
import com.ljx.enumeration.ResponseCode;
import com.ljx.protection.RateLimiter;
import com.ljx.protection.TokenBuketRateLimiter;
import com.ljx.transport.message.RequestPayload;
import com.ljx.transport.message.RpcRequest;
import com.ljx.transport.message.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());
        Channel channel = channelHandlerContext.channel();

        if(ShutDownHolder.BLOCKER.get()){
            log.error("服务端已经关闭，拒绝请求！");
            rpcResponse.setCode(ResponseCode.BECLOSEING.getCode());
            channel.writeAndFlush(rpcResponse);
            return;
        }
        ShutDownHolder.REQUEST_COUNTER.increment();

        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = RpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if(rateLimiter == null){
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        Boolean allowRequest = rateLimiter.allowRequest();
        //处理心跳
        if(rpcRequest.getRequestType()== RequestType.HEART_BEAT.getId()){
            rpcResponse.setCode(ResponseCode.SUCCESS_HEARTBEAT.getCode());
        }
        //限流
        else if(!allowRequest){
            rpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
        }
        else {
            //1.获得负载内容
            RequestPayload requestPayload = rpcRequest.getRequestPayload();
            //2.根据负载内容进行方法调用
            try{
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成了方法调用。", rpcRequest.getRequestId());
                }
                //3.封装响应对象
                rpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                rpcResponse.setBody(result);
            } catch(Exception ex){
                rpcResponse.setCode(ResponseCode.FAIL.getCode());
                log.error("调用服务【{}】的【{}】的方法时发生了异常！",requestPayload.getInterfaceName(),requestPayload.getMethodName());
            }
        }
        //4.写出响应
        channel.writeAndFlush(rpcResponse);
        ShutDownHolder.REQUEST_COUNTER.decrement();
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parameterTypes = requestPayload.getParameterTypes();
        Object[] parameters = requestPayload.getParameterValues();
        //寻找合适的类完成方法调用
        ServiceConfig<?> serviceConfig = RpcBootstrap.SERVICES_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        //反射调用 1.获取方法对象 2.执行invoke方法
        Object returnValue = null;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parameterTypes);
            returnValue = method.invoke(refImpl, parameters);
        } catch (IllegalAccessException|NoSuchMethodException|InvocationTargetException ex) {
            log.error("调用服务【{}】的【{}】的方法时发生了异常！",requestPayload.getInterfaceName(),requestPayload.getMethodName());
            throw new RuntimeException(ex);
        }
        return returnValue;
    }
}
