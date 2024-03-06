package com.ljx.utils.zookeeper;

import com.ljx.Constant;
import com.ljx.Exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
@Slf4j
public class ZookeeperUtil {
    /**
     * 创建zookeeper实例
     * @param connectString 连接地址
     * @param timeout 超时时间
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(String connectString , int timeout){
        //定义一个计数器，用于等待zookeeper连接成功
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            //创建zoopkeeper实例，创建连接
            final ZooKeeper zookeeper = new ZooKeeper(connectString, timeout, watchedEvent -> {
                //连接成功才放行
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("zookeeper客户端连接成功！");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zookeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常：{}", e.getMessage());
            throw new ZookeeperException();
        }
    }
    public static ZooKeeper createZookeeper(){
        //定义连接参数
        String connectString = Constant.DEFAULT_ZK_ADDRESS;
        //定义超时时间
        int timeout = Constant.DEFAULT_ZK_SESSION_TIMEOUT;
        return createZookeeper(connectString,timeout);
    }
    /**
     * 关闭zookeeper连接
     * @param zookeeper zookeeper实例
     */
    public static void closeZookeeper(ZooKeeper zookeeper) {
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper连接时发生异常：{}", e.getMessage());
            throw new ZookeeperException();
        }
    }
    /**
     * 创建一个节点的工具方法
     * @param zookeeper zookeeper实例
     * @param node 节点
     * @param watcher 监听器
     * @param createMode 节点类型
     * @return true:创建成功，false:已存在 exception:创建失败
     */
    public static boolean createNode(ZooKeeper zookeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode){
        try {
            if(zookeeper.exists(node.getNodePath(), watcher) == null){
                String result = zookeeper.create(node.getNodePath(), node.getData(), org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("创建节点成功，节点路径为：{}", result);
                return true;
            } else {
                if(log.isDebugEnabled()){
                    log.debug("节点已存在，节点路径为：{},无需创建", node.getNodePath());
                }
            }
        } catch (InterruptedException | KeeperException e) {
            log.error("创建节点时发生异常：{}", e.getMessage());
            throw new ZookeeperException();
        }
        return false;
    }

    /**
     * 判断节点是否存在
     * @param node 节点路径
     * @param watcher 监听器
     * @param zookeeper zookeeper实例
     * @return true:存在，false:不存在
     */
    public static boolean exists(String node,Watcher watcher,ZooKeeper zookeeper){
        try {
            return zookeeper.exists(node, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点是否存在时发生异常：{}", e.getMessage());
            throw new ZookeeperException();
        }
    }

    /**
     * 获取某个节点的子节点
     * @param parentNode 父节点
     * @param zooKeeper zookeeper实例
     * @return 子节点数组
     */
    public static List<String> getChildren(String parentNode, ZooKeeper zooKeeper, Watcher watcher){
        try {
            return zooKeeper.getChildren(parentNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取【{}】子节点时发生异常：{}", parentNode ,e.getMessage());
            throw new ZookeeperException();
        }
    }
}

