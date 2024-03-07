package com.ljx.discovery.impl;

import com.ljx.Constant;
import com.ljx.Exceptions.DiscoveryException;
import com.ljx.Exceptions.NetworkException;
import com.ljx.RpcBootstrap;
import com.ljx.ServiceConfig;
import com.ljx.discovery.AbstractRegistry;
import com.ljx.utils.NetUtils;
import com.ljx.utils.zookeeper.ZookeeperNode;
import com.ljx.utils.zookeeper.ZookeeperUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
@Slf4j

public class ZookeeperRegistry extends AbstractRegistry {
    private ZooKeeper zooKeeper = ZookeeperUtil.createZookeeper();

    public ZookeeperRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        //这个节点应该是一个持久节点
        if(!ZookeeperUtil.exists(parentNode,null,zooKeeper)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(parentNode, null), null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点,ip:port
        String localIp = NetUtils.getLocalIp();
        //todo:后续处理端口问题
        String localNode = parentNode + "/" + localIp + ":" + RpcBootstrap.PORT;
        if(!ZookeeperUtil.exists(localNode,null,zooKeeper)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(localNode, null), null, CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()){
            log.debug("服务{}已经被注册", service.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String name) {
        //1.找到服务对应的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + name;
        //2.从zk中获取他的子节点
        List<String> children = ZookeeperUtil.getChildren(parentNode, zooKeeper,null);
        //3.解析子节点的数据，返回ip和端口
        List<InetSocketAddress> addresses = new ArrayList<>();
        children.stream().map(ipString-> {
            String[] split = ipString.split(":");
            return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
        }).forEach(addresses::add);
        if(addresses.isEmpty()||addresses.size()==0){
            throw new DiscoveryException("未发现任何的服务的可用主机");
        }
        // todo: 1.每次调用相关方法都需要去注册中心拉取服务列表吗？ 本地缓存 ＋ watch机制
        //       2.如何合理的选择一个可用的服务，而不是选择第一个 负载均衡
        return addresses;
    }
}
