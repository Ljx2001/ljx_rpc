package com.ljx;

import com.ljx.ChannelHandler.handler.MethodCallHandler;
import com.ljx.ChannelHandler.handler.RpcRequestDecoderHandler;
import com.ljx.ChannelHandler.handler.RpcResponseEncoderHandler;
import com.ljx.annotation.RpcService;
import com.ljx.config.Configuration;
import com.ljx.core.HeartbeatDetector;
import com.ljx.discovery.RegistryConfig;
import com.ljx.loadbalancer.LoadBalancer;
import com.ljx.transport.message.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
public class RpcBootstrap {
    //RpcBootStrap是个单例，每个应用程序中只有一个实例
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    //全局的配置中心
    private Configuration configuration = new Configuration();
    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    //服务端响应时间的缓存
    public final static TreeMap<Long, Channel> CHANNEL_ANSWER_TIME_CACHE = new TreeMap<>();
    //维护已经发布且暴露的服务列表，key是interface的全限定名，value是服务的配置
    public static final Map<String,ServiceConfig<?>> SERVICES_LIST = new ConcurrentHashMap<>();
    //定义全局的对外挂起的CompletableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();
    //保存RpcRequest对象，可以在当前线程中随时获取
    public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    private RpcBootstrap() {
        //构造启动引导程序时需要做一些什么初始化的事
        configuration = new Configuration();
    }

    /**
     * 获取RpcBootstrap实例
     * @return RpcBootstrap实例
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 用来定义当前应用的名字
     * @return this
     */
    public RpcBootstrap application(String appName) {
        //设置应用的名字
        configuration.setApplicationName(appName);
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }
    /**
     * 用来配置一个负载均衡器
     * @return this
     */
    public RpcBootstrap registry(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 用来配置一个序列化协议
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
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
        HeartbeatDetector.detectHeartbeat(reference.getInterfaceRef().getName());
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
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
        configuration.getRegistryConfig().getRegistry().register(service);
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
                .localAddress(new InetSocketAddress(configuration.getPort()))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加日志处理器
                        ch.pipeline().addLast(new LoggingHandler())
                                //添加报文解码处理器
                                .addLast(new RpcRequestDecoderHandler())
                                //添加方法调用处理器
                                .addLast(new MethodCallHandler())
                                //添加响应编码器
                                .addLast(new RpcResponseEncoderHandler());
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

    /**
     * 用来配置序列化的方式
     * @param serializeType 序列化的方式
     * @return this
     */
    public RpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("该工程配置了序列化方式为【{}】。",serializeType);
        }
        return this;
    }
    public RpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("该工程配置了压缩方式为【{}】。",compressType);
        }
        return this;
    }

    /**
     * 扫描并发布指定包下的服务
     * @param packageName
     * @return
     */
    public RpcBootstrap scan(String packageName) {
        //通过packageName获取到其下所有类的全限定名
        List<String> classNames = getAllClassNames(packageName);
        //通过反射获取他的接口，构建具体实现
        List<? extends Class<?>> classes = classNames.stream()
                .map(className -> {
                    try{
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("类扫描时异常");
                    }
                }).filter(clazz -> clazz.getAnnotation(RpcService.class)!=null).collect(Collectors.toList());
        for (Class<?> aClass : classes) {
            Class<?>[] interfaces = aClass.getInterfaces();
            Object instance = null;
            try {
                instance = aClass.getConstructor().newInstance();

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                //发布服务
                this.publish(serviceConfig);
                if(log.isDebugEnabled()){
                    log.debug("扫描到服务：【{}】,并发布}",anInterface.getName());
                }
            }
        }
        return this;
    }
    /**
     * 扫描指定包下的所有类的全限定名称
     * @param packageName
     * @return
     */
    private List<String> getAllClassNames(String packageName) {
        //1.通过packageName获得绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        System.out.println(basePath);
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            throw new RuntimeException("包扫描时，发现路径不存在");
        }
        String absolutePath = url.getPath();
        System.out.println(absolutePath);
        List<String> classNames = new ArrayList<>();
        List<String> allClassNames = recursionFile(absolutePath,classNames,basePath);
        return allClassNames;
    }

    /**
     * 递归获取指定路径下的所有类的全限定名
     *
     * @param absolutePath
     * @param classNames
     * @param basePath
     * @return
     */
    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        //获取文件
        File file = new File(absolutePath);
        //判断是否是文件夹
        if(file.isDirectory()) {
            //获取文件夹下的所有文件
            File[] childfiles = file.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".class"));
            for (File f : childfiles) {
                if(f.isDirectory()){
                    recursionFile(f.getAbsolutePath(), classNames, basePath);
                }else{
                    String className = getClassNameByAbsolutePath(f.getAbsolutePath(), basePath);
                    if(className != null){
                        classNames.add(className);
                    }
                }
            }
        }else{
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            if(className != null){
                classNames.add(className);
            }
        }
        return classNames;
    }
    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\", ".");
        return fileName.substring(0, fileName.indexOf(".class"));
    }

    public static void main(String[] args) {
        RpcBootstrap.getInstance().getAllClassNames("com.ljx");
    }
}

