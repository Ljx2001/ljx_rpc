package com.ljx;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ZookeeperTest {
    ZooKeeper zookeeper;
    @Before
    public void start() throws IOException {
        //定义连接参数
        String connectString = "localhost:2181";
        //定义超时时间
        int timeout = 10000;
        zookeeper = new ZooKeeper(connectString, timeout, new MyWatcher());
    }
    @Test
    public void testCreateNode() throws Exception {
        //创建节点
        String result = zookeeper.create("/ljx", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(result);
    }
    @Test
    public void testDeleteNode() throws Exception {
        zookeeper.setData("/ljx", "hello".getBytes(),-1);
        Stat stat = zookeeper.exists("/ljx", true);
        //获取节点的数据版本
        int version = stat.getVersion();
        //获取节点的acl版本
        int aclVersion = stat.getAversion();
        //获取节点的模式版本
        int cversion = stat.getCversion();
        System.out.println("version:" + version + ",aclVersion:" + aclVersion + ",cversion:" + cversion);
        //删除节点
        zookeeper.delete("/ljx", -1);
    }
    @Test
    public void testWatcher() throws Exception {
        //注册监听器
        zookeeper.exists("/ljx", new MyWatcher());
        while(true){
            Thread.sleep(1000);
        }
    }
}
