package com.ljx;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.ResolvedModule;
import java.util.List;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
public class RpcBootstrap {
    //private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);
    //RpcBootStrap是个单例，每个应用程序中只有一个实例
    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

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
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 用来配置一个协议
     *
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }
    /**
     * 用来配置服务
     * @return this
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }
    /**
     * 用来发布服务
     * @param service 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        if(log.isDebugEnabled()){
            log.debug("服务{}已经被注册", service.getInterface().getName());
        }
        return this;
    }
    /**
     * 用来批量发布服务
     * @param services 封装的需要发布的服务的列表
     * @return this
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        return this;
    }


    public void start() {
    }
}

