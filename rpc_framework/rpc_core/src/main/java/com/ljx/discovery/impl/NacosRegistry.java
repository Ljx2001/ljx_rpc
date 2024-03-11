package com.ljx.discovery.impl;

import com.ljx.ServiceConfig;
import com.ljx.discovery.AbstractRegistry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
public class NacosRegistry extends AbstractRegistry {
    public NacosRegistry(String host, int defaultZkSessionTimeout) {
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> lookup(String name,String group) {
        return null;
    }
}
