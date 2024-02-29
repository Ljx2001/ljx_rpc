package com.ljx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

public class MyChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf= (ByteBuf) msg;
        System.out.println("server receive message : " + byteBuf.toString(Charset.forName("UTF-8")));
        ctx.writeAndFlush(Unpooled.copiedBuffer("message has been received".getBytes(Charset.forName("UTF-8"))));
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //打印异常信息并关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
