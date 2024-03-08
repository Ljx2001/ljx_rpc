package com.ljx.core;

import com.ljx.NettyBootstrapInitalizer;
import com.ljx.RpcBootstrap;
import com.ljx.compress.CompressorFactory;
import com.ljx.discovery.Registry;
import com.ljx.enumeration.RequestType;
import com.ljx.serialize.SerializerFactory;
import com.ljx.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author LiuJixing
 * @Date 7/3/2024
 */
@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String ServiceName){
        //从注册中心拉取服务列表并建立连接
        Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(ServiceName);

        //将连接进行缓存
        for (InetSocketAddress address : addresses) {
            try {
                if(!RpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = NettyBootstrapInitalizer.getBootstrap().connect(address).sync().channel();
                    RpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //定期发送心跳包
        Thread thread = new Thread(()->{
            new Timer().scheduleAtFixedRate(new MyTimerTask(),0,3000);
        },"rpc-HeartbeatDetector-Thread");
        thread.setDaemon(true);
        thread.start();
    }
    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            //将响应时间缓存清空
            RpcBootstrap.CHANNEL_ANSWER_TIME_CACHE.clear();
            //遍历所有的channel，发送心跳包
            for (Channel channel : RpcBootstrap.CHANNEL_CACHE.values()) {
                int tryTimes = 3;
                while(tryTimes>0){
                    long start = System.currentTimeMillis();
                    //构建一个心跳请求
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .requestId(RpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEART_BEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .timestamp(start)
                            .build();
                    //写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    //将CompletableFuture放入全局的缓存中，暴露出去
                    RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
                    //将RpcRequest写出到pipeline，供责任链上的handler进行一系列出栈的处理
                    channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                        if(!promise.isSuccess()) {
                            //捕获异步任务中的异常
                            Throwable cause = promise.cause();
                            completableFuture.completeExceptionally(cause);
                        }
                    });

                    Long endTime = null;
                    try {
                        completableFuture.get(1000, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        tryTimes--;
                        log.error("和主机【{}】的连接发生异常,开始重试",channel.remoteAddress());
                        if(tryTimes==0){
                            //将失效的地址移出服务列表
                            log.error("主机【{}】的连接断开。",channel.remoteAddress());
                            RpcBootstrap.CHANNEL_CACHE.remove(channel.remoteAddress());
                        }
                        try {
                            Thread.sleep(50*(new Random().nextInt(10)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    //使用treeMap缓存响应时间，并排序
                    RpcBootstrap.CHANNEL_ANSWER_TIME_CACHE.put(time,channel);
                    log.debug("和【{}】服务器的响应时间是【{}】。",channel.remoteAddress(),time);
                    break;
                }
            }
            log.debug("-------------------------------------响应时间的treemap--------------------------------------");
            for(Long time : RpcBootstrap.CHANNEL_ANSWER_TIME_CACHE.keySet()){
                if(log.isDebugEnabled()){
                    log.debug("响应时间【{}】-->地址【{}】",time,RpcBootstrap.CHANNEL_ANSWER_TIME_CACHE.get(time).remoteAddress());
                }
            }
        }
    }
}
