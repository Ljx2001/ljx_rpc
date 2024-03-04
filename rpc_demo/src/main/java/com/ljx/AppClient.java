package com.ljx;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class AppClient implements Serializable {

    public void run(){
        //定义一个线程组，用于处理网络事件
        NioEventLoopGroup group = new NioEventLoopGroup();
        //创建一个客户端启动辅助类
        Bootstrap bootstrap = new Bootstrap();
        //给启动类配置线程组、通道类型、地址、处理器
        bootstrap = bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加处理器
                        ch.pipeline().addLast(new MyClientChannelHandler());
                    }
                });

        try {
            //连接服务器
            ChannelFuture future = bootstrap.connect().sync();
            System.out.println("connected to server");
            //获取连接通道，并写入数据，发送到服务器
            future.channel().writeAndFlush(Unpooled.copiedBuffer("hello server".getBytes(Charset.forName("UTF-8"))));
            //阻塞，直到连接关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭线程组
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppClient().run();
    }


}
