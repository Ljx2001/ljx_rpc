package com.ljx.discovery;

import com.ljx.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 抽象注册中心接口
 * @Author LiuJixing
 * @Date 4/3/2024
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 服务配置
     */
    public void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param name 服务的名称
     * @return 服务的ip和端口
     */
    InetSocketAddress lookup(String name);
}
