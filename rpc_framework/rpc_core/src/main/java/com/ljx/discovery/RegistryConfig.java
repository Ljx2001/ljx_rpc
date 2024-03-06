package com.ljx.discovery;

import com.ljx.Constant;
import com.ljx.Exceptions.DiscoveryException;
import com.ljx.discovery.Registry;
import com.ljx.discovery.impl.NacosRegistry;
import com.ljx.discovery.impl.ZookeeperRegistry;


/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
public class RegistryConfig {
    //定义连接的url zookeeper://127.0.0.1：2181
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    public Registry getRegistry() {
        //根据连接地址的类型，返回不同的注册中心实现
        String registryType = getRegistryTypeOrHost(connectString,true).toLowerCase().trim();
        if(registryType.equals("zookeeper")) {
            String host = getRegistryTypeOrHost(connectString,false);
            return new ZookeeperRegistry(host, Constant.DEFAULT_ZK_SESSION_TIMEOUT);
        } else if (registryType.equals("nacos")) {
            String host = getRegistryTypeOrHost(connectString,false);
            return new NacosRegistry(host, Constant.DEFAULT_ZK_SESSION_TIMEOUT);
        }
        throw new DiscoveryException("未发现支持的注册中心类型。");
    }
    private String getRegistryTypeOrHost(String connectString,boolean ifType) {
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2) {
            throw new IllegalArgumentException("Wrong registry address format");
        }
        if(ifType)
            return typeAndHost[0];
        else
            return typeAndHost[1];
    }
}
