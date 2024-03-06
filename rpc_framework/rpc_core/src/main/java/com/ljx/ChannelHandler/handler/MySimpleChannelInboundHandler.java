package com.ljx.ChannelHandler.handler;

import com.ljx.RpcBootstrap;
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
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        String result = msg.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
        log.info("客户端收到消息：{}", msg.toString(Charset.defaultCharset()));
    }
}
