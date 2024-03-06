package com.ljx;

import com.ljx.ChannelHandler.ConsumerChannelInitializer;
import com.ljx.ChannelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供Netty的Bootstrap单例
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Slf4j
public class NettyBootstrapInitalizer {
    //定义唯一的Netty服务端启动类
    private static final Bootstrap bootstrap = new Bootstrap();

    static{
        //定义唯一的线程组，用于处理网络事件
        NioEventLoopGroup group = new NioEventLoopGroup();
        //给启动类配置线程组、通道类型、地址、处理器
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }
    private  NettyBootstrapInitalizer() {
    }
    public static Bootstrap getBootstrap() {
        //给启动类配置线程组、通道类型、地址、处理器
        return bootstrap;
    }
}
