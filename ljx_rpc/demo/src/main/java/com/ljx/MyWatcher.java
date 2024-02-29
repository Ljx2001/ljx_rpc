package com.ljx;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        //判断事件类型
        //首先判断是否是连接类型的事件
        if (event.getType() == Event.EventType.None){
            //判断是否连接成功
            if (event.getState() == Event.KeeperState.SyncConnected){
                System.out.println("zookeeper连接成功");
            } else if (event.getState() == Event.KeeperState.Disconnected){
                System.out.println("zookeeper连接断开");
            } else if (event.getState() == Event.KeeperState.Expired){
                System.out.println("zookeeper连接超时");
            } else if (event.getState() == Event.KeeperState.AuthFailed){
                System.out.println("zookeeper连接认证失败");
            }
        } else if (event.getType() == Event.EventType.NodeCreated){
            System.out.println("节点创建");
        } else if (event.getType() == Event.EventType.NodeDeleted){
            System.out.println("节点删除");
        } else if (event.getType() == Event.EventType.NodeDataChanged){
            System.out.println("节点数据变更");
        } else if (event.getType() == Event.EventType.NodeChildrenChanged){
            System.out.println("子节点变更");
        }
    }
}
