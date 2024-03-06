package com.ljx.ChannelHandler.handler;

import com.ljx.RpcBootstrap;
import com.ljx.ServiceConfig;
import com.ljx.transport.message.RequestPayload;
import com.ljx.transport.message.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        //1.获得负载内容
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        //2.根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        //3.封装响应
        //4.写出响应
        channelHandlerContext.writeAndFlush(null);
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
