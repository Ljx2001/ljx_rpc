package com.ljx.loadbalancer;

import com.ljx.RpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{
    private Map<String,Selector> selectorCache = new ConcurrentHashMap<>();
    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        //先从缓存中获取选择器
        Selector selector = selectorCache.get(serviceName);
        if(selector==null){
            //如果缓存中没有，就创建一个新的，并放入缓存
            List<InetSocketAddress> serviceList = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            selectorCache.put(serviceName,selector);
        }
        return selector.getNext();
    }
    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> serviceList) {
        //根据新的服务列表，生成新的选择器，并放入缓存
        System.out.println("------------------------------------------------------------------------------------");
        selectorCache.put(serviceName,getSelector(serviceList));
    }

    /**
     * 由子类进行扩展，选择器的创建
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
