package com.ljx.discovery.impl;

import com.ljx.ServiceConfig;
import com.ljx.discovery.AbstractRegistry;

import java.net.InetSocketAddress;

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
    public InetSocketAddress lookup(String name) {
        return null;
    }
}
