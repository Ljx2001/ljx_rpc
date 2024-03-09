package com.ljx.config;

import com.ljx.compress.Compressor;
import com.ljx.compress.CompressorFactory;
import com.ljx.loadbalancer.LoadBalancer;
import com.ljx.serialize.Serializer;
import com.ljx.serialize.SerializerFactory;
import com.ljx.spi.SpiHandler;

import java.util.List;

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
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrapers = SpiHandler.getList(LoadBalancer.class);
        if(loadBalancerWrapers != null && !loadBalancerWrapers.isEmpty()) {
            configuration.setLoadBalancer(loadBalancerWrapers.get(0).getImpl());
        }
        List<ObjectWrapper<Compressor>> compressorWrappers = SpiHandler.getList(Compressor.class);
        if(compressorWrappers != null && !compressorWrappers.isEmpty()) {
            compressorWrappers.forEach(CompressorFactory::addCompressor);
        }
        List<ObjectWrapper<Serializer>> serializerWrappers = SpiHandler.getList(Serializer.class);
        if(serializerWrappers != null && !serializerWrappers.isEmpty()) {
            serializerWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
