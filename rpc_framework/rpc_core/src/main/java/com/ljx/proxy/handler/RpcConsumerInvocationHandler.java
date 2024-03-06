package com.ljx.proxy.handler;

import com.ljx.Exceptions.DiscoveryException;
import com.ljx.Exceptions.NetworkException;
import com.ljx.NettyBootstrapInitalizer;
import com.ljx.RpcBootstrap;
import com.ljx.discovery.Registry;
import com.ljx.transport.message.RequestPayload;
import com.ljx.transport.message.RpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑，代理对象的远程调用过程封装在invoke方法中
 * 1.发现可用服务 2.建立连接 3.发送请求 4.得到结果
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    private Class<?> interfaceRef;
    private Registry registry;
    public RpcConsumerInvocationHandler(Class<?> interfaceRef, Registry registry) {
        this.interfaceRef = interfaceRef;
        this.registry = registry;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1.发现服务，从注册中心寻找一个可用的服务
        //传入服务的名字，返回ip和端口
        InetSocketAddress addr =  registry.lookup(interfaceRef.getName());
        if(log.isDebugEnabled()){
            log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), addr);
        }
        //2.得到一个可用的通道
        Channel channel = getAvailableChannel(addr);
        if(log.isDebugEnabled()){
            log.debug("获取了和【{}】的连接通道，准备放松数据", channel);
        }
        //3.封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterValues(args)
                .parameterTypes(method.getParameterTypes())
                .returnType(method.getReturnType())
                .build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(1L)
                .compressType((byte) 1)
                .requestType((byte) 1)
                .serializeType((byte) 1)
                .requestPayload(requestPayload)
                .build()




        ;

        //------------同步策略--------------
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
//                if(channelFuture.isDone()) {
//                    Object object = channelFuture.getNow();
//                } else if(!channelFuture.isSuccess()) {
//                    //捕获异步任务中的异常
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }
        //------------异步策略--------------
        //4.写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        //将CompletableFuture放入全局的缓存中，暴露出去
        RpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        //将RpcRequest写出到pipeline，供责任链上的handler进行一系列出栈的处理
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
            if(!promise.isSuccess()) {
                //捕获异步任务中的异常
                Throwable cause = promise.cause();
                completableFuture.completeExceptionally(cause);
            }
        });
        //5.等待获取相应的结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 根据服务的地址获取一个可用的通道
     * @param addr
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress addr) {
        //尝试从全局的缓存中获取一个通道
        Channel channel = RpcBootstrap.CHANNEL_CACHE.get(addr);
        if(channel == null || !channel.isOpen()){
            //如果没有获取到，就创建一个新的通道
            CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
            try {
                NettyBootstrapInitalizer.getBootstrap().connect(addr).await().addListener((ChannelFutureListener) promise -> {
                    if(promise.isDone()){
                        completableFuture.complete(promise.channel());
                        log.info("与【{}】成功建立连接", addr);
                    }
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                channel = completableFuture.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException|ExecutionException|InterruptedException e) {
                log.error("建立与【{}】的通道时发生异常。", addr);
                throw new DiscoveryException(e);
            }
            RpcBootstrap.CHANNEL_CACHE.put(addr, channel);
        }
        if (channel == null) {
            log.error("获取或建立与【{}】的通道时发生异常。", addr);
            throw new NetworkException();
        }
        return channel;
    }
}
