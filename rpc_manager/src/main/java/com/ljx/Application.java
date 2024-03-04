package com.ljx;



import com.ljx.Exceptions.ZookeeperException;
import com.ljx.utils.zookeeper.ZookeeperNode;
import com.ljx.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;

/**
 * 注册中心的管理页面
 * @Author LiuJixing
 * @Date 4/3/2024
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //创建zoopkeeper实例，创建连接
        final ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();
        //定义节点
        ZookeeperNode baseNode = new ZookeeperNode(Constant.BASE_PATH, null);
        ZookeeperNode providersNode = new ZookeeperNode(Constant.BASE_PROVIDER_PATH, null);
        ZookeeperNode consumersNode = new ZookeeperNode(Constant.BASE_CONSUMER_PATH, null);
        //创建基础目录
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtil.createNode(zookeeper, node, null, org.apache.zookeeper.CreateMode.PERSISTENT);
        });
        //关闭连接
        ZookeeperUtil.closeZookeeper(zookeeper);

    }
}
