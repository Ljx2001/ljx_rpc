package com.ljx;

import com.ljx.ChannelHandler.handler.MethodCallHandler;
import com.ljx.ChannelHandler.handler.RpcMessageDecoderHandler;
import com.ljx.discovery.Registry;
import com.ljx.discovery.RegistryConfig;
import com.ljx.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
public class RpcBootstrap {
    //private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);
    //RpcBootStrap是个单例，每个应用程序中只有一个实例
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;
    private Registry registry;
    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    //维护已经发布且暴露的服务列表，key是interface的全限定名，value是服务的配置
    public static final Map<String,ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>();
    //定义全局的对外挂起的CompletableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();

    private RpcBootstrap() {
        //构造启动引导程序时需要做一些什么初始化的事
    }

    /**
     * 获取RpcBootstrap实例
     *
     * @return RpcBootstrap实例
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     *
     * @return this
     */
    public RpcBootstrap application(String appName) {
        //设置应用的名字
        this.applicationName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 用来配置一个协议
     *
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     * ---------------------------------服务消费方相关的api---------------------------------
     */
    /**
     * 用来配置服务
     * @return this
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(registry);
        return this;
    }
    /**
     * ---------------------------------服务提供方相关的api---------------------------------
     */

    /**
     * 用来发布服务,将接口对应的实现类注册到注册中心
     * @param service 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        //抽象了注册中心的概念，使用注册中心的一个实现的register方法将服务注册到注册中心
        registry.register(service);
        SERVICES_LIST.put(service.getInterface().getName(),service);
        return this;
    }
    /**
     * 用来批量发布服务
     * @param services 封装的需要发布的服务的列表
     * @return this
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }


    public void start() {
        //创建eventLoopGroup，老板只负责处理请求，然后把请求转给worker
        NioEventLoopGroup boss = new NioEventLoopGroup();
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
                        //添加日志处理器
                        ch.pipeline().addLast(new LoggingHandler())
                                //添加报文解码处理器
                                .addLast(new RpcMessageDecoderHandler())
                                ///添加方法调用处理器
                                .addLast(new MethodCallHandler());
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
        String ip = NetUtils.getLocalIp();
        System.out.println(ip);
    }
}

