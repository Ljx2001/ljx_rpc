package com.ljx.loadbalancer.impl;

import com.ljx.Exceptions.LoadBalancerException;
import com.ljx.RpcBootstrap;
import com.ljx.loadbalancer.AbstractLoadBalancer;
import com.ljx.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author LiuJixing
 * @Date 7/3/2024
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }
    private static class MinimumResponseTimeSelector implements Selector {
        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {
        }
        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long,Channel> entry = RpcBootstrap.CHANNEL_ANSWER_TIME_CACHE.firstEntry();
            if(entry!=null){
                Channel channel = entry.getValue();
                InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
                if(log.isDebugEnabled()){
                    log.debug("选择了响应时间为【{}】ms的服务节点。",entry.getKey());
                }
                return addr;
            }
            //如果从应答时间缓存中找不到，就从通道缓存中随机拿一个
            int Channels_num = RpcBootstrap.CHANNEL_CACHE.size();
            Random random = new Random(1000);
            int randomNumber = random.nextInt(Channels_num);
            Channel channel = (Channel) RpcBootstrap.CHANNEL_CACHE.values().toArray()[randomNumber];
            return (InetSocketAddress) channel.remoteAddress();
        }
    }
}
