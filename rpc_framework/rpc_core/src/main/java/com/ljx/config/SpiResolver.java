package com.ljx.config;

import com.ljx.compress.Compressor;
import com.ljx.loadbalancer.LoadBalancer;
import com.ljx.serialize.Serializer;
import com.ljx.spi.SpiHandler;

/**
 * @Author LiuJixing
 * @Date 8/3/2024
 */
public class SpiResolver {
    /**
     * 通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        configuration.setLoadBalancer(loadBalancer);
        Compressor compressor = SpiHandler.get(Compressor.class);
        configuration.setCompressor(compressor);
        Serializer serializer = SpiHandler.get(Serializer.class);
        configuration.setSerializer(serializer);
        
    }
}
