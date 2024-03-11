package com.ljx.watcher;

import com.ljx.NettyBootstrapInitalizer;
import com.ljx.RpcBootstrap;
import com.ljx.discovery.Registry;
import com.ljx.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @Author LiuJixing
 * @Date 7/3/2024
 */
@Slf4j
public class NodeUPAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if(log.isDebugEnabled()){
            log.debug("检测到服务【{}】有节点上线或者下线，将重新拉取服务列表", watchedEvent.getPath());
        }
        String serviceName = getServiceName(watchedEvent.getPath());
        Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addrs = registry.lookup(serviceName,RpcBootstrap.getInstance().getConfiguration().getGroup());
        for (InetSocketAddress addr : addrs) {
            //新增的节点，需要建立连接
            if(!RpcBootstrap.CHANNEL_CACHE.containsKey(addr)){
                Channel channel = null;
                try {
                    channel = NettyBootstrapInitalizer.getBootstrap().connect(addr).sync().channel();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                RpcBootstrap.CHANNEL_CACHE.put(addr, channel);
            }
        }
        for (Map.Entry<InetSocketAddress,Channel> entry:RpcBootstrap.CHANNEL_CACHE.entrySet()){
            if(!addrs.contains(entry.getKey())){
                //已经下线的节点，需要关闭连接
                RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
            }
        }
        //获得负载均衡器，重新reloadBalance
        LoadBalancer  loadBalancer = RpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
        loadBalancer.reLoadBalance(serviceName,addrs);

    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
