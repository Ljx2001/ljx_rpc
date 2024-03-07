package com.ljx.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public interface Selector {
    /**
     * 从服务列表中执行算法选择一个服务
     * @param
     * @return 选择的服务地址
     */
    InetSocketAddress getNext();
    //todo 服务下线重新做负载均衡
    void reBalance();
}
