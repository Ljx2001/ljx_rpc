package com.ljx;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class AppServer {
    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start(){
        //创建eventLoopGroup，老板只负责处理请求，然后把请求转给worker
        NioEventLoopGroup boss= new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        //创建一个服务端启动辅助类
        ServerBootstrap bootstrap = new ServerBootstrap();
        //给启动类配置线程组、通道类型、地址、处理器
        bootstrap = bootstrap.group(boss, worker)
                .localAddress(new InetSocketAddress(port))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加处理器
                        ch.pipeline().addLast(new MyChannelHandler());
                    }
                });
        try {
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind().sync();
            //阻塞，直到服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭线程组
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppServer(8080).start();
    }
}
