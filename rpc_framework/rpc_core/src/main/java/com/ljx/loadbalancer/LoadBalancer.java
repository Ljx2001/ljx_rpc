package com.ljx.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public interface LoadBalancer {
    /**
     * 从服务名中选择一个服务
     * @param serviceName 服务名称
     * @return 选择的服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
