package com.ljx.proxy.handler;

import com.ljx.Exceptions.CircuitOpenException;
import com.ljx.annotation.TryTimes;
import com.ljx.compress.CompressorFactory;
import com.ljx.protection.CircuitBreaker;
import com.ljx.serialize.SerializerFactory;
import com.ljx.Exceptions.DiscoveryException;
import com.ljx.Exceptions.NetworkException;
import com.ljx.NettyBootstrapInitalizer;
import com.ljx.RpcBootstrap;
import com.ljx.discovery.Registry;
import com.ljx.enumeration.RequestType;
import com.ljx.transport.message.RequestPayload;
import com.ljx.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
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
    private String group;
    public RpcConsumerInvocationHandler(Class<?> interfaceRef, Registry registry,String group) {
        this.interfaceRef = interfaceRef;
        this.registry = registry;
        this.group = group;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);
        int tryTimes = 0;
        int intervalTime = 0;
        if(tryTimesAnnotation != null){
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.interval();
        }
        int initTryTimes = tryTimes;
        while (true) {
            try {
                //1.封装报文
                RequestPayload requestPayload = RequestPayload.builder()
                        .interfaceName(interfaceRef.getName())
                        .methodName(method.getName())
                        .parameterValues(args)
                        .parameterTypes(method.getParameterTypes())
                        .returnType(method.getReturnType())
                        .build();
                RpcRequest rpcRequest = RpcRequest.builder()
                        .requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                        .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                        .requestType(RequestType.REQUEST.getId())
                        .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                        .timestamp(System.currentTimeMillis())
                        .requestPayload(requestPayload)
                        .build();
                RpcBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);
                //2.发现服务，从注册中心拉取服务列表，并通过客户端负载均衡寻找一个可用的服务
                //获取当前配置的负载均衡器，选取一个可用节点
                InetSocketAddress addr = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName(),group);
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), addr);
                }

                //获取当前IP对应的断路器
                Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = RpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
                CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(addr);
                if(circuitBreaker == null){
                    circuitBreaker = new CircuitBreaker(10,0.2F);
                    everyIpCircuitBreaker.put(addr, circuitBreaker);
                }

                //如果熔断器是开启状态
                if(circuitBreaker.isBreak()){
                    //开启一个定时器，5秒后重置熔断器
                    Timer timer = new Timer();
                    timer.schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            RpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker().get(addr).reset();
                        }
                    }, 5000);
                    throw new CircuitOpenException("当前熔断器在开启状态，无法发送请求。");
                }

                //3.得到一个可用的通道
                Channel channel = getAvailableChannel(addr);
                if (log.isDebugEnabled()) {
                    log.debug("获取了和【{}】的连接通道，准备发送数据", channel);
                }

                //4.写出报文
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                //将CompletableFuture放入全局的缓存中，暴露出去
                RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
                //将RpcRequest写出到pipeline，供责任链上的handler进行一系列出栈的处理
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        //捕获异步任务中的异常
                        Throwable cause = promise.cause();
                        completableFuture.completeExceptionally(cause);
                    }
                });
                //清理ThreadLocal
                RpcBootstrap.REQUEST_THREAD_LOCAL.remove();
                //5.等待获取相应的结果
                Object result = completableFuture.get(100, TimeUnit.SECONDS);
                circuitBreaker.record(true);
                return result;
            } catch (DiscoveryException | InterruptedException | ExecutionException | TimeoutException e) {
                //执行重试
                tryTimes--;
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("在进行重试时发生了异常", ex);
                }
                if(tryTimes < 0) {
                    log.error("在对方法【{}】进行【{}】次调用重试仍出错", method.getName(),initTryTimes, e);
                    break;
                } else{

                }
                log.error("在进行第【{}】重试时发生了异常", 3 - tryTimes, e);
            } catch (CircuitOpenException e){
                log.error("当前熔断器在开启状态，无法发送请求。", e);
                break;
            }
        }
        log.error("在执行远程方法"+method.getName()+"调用时时发生了异常");
        return null;
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
