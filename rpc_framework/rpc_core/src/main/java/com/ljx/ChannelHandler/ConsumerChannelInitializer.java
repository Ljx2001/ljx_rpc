package com.ljx.ChannelHandler;

import com.ljx.ChannelHandler.handler.MySimpleChannelInboundHandler;
import com.ljx.ChannelHandler.handler.RpcMessageEncoderHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author LiuJixing
 * @Date 5/3/2024
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //添加处理器
        ch.pipeline()
                //netty提供的日志处理器，可以打印日志
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new RpcMessageEncoderHandler())
                .addLast(new MySimpleChannelInboundHandler());

    }
}
