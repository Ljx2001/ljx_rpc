package com.ljx.loadbalancer.impl;

import com.ljx.RpcBootstrap;
import com.ljx.loadbalancer.AbstractLoadBalancer;
import com.ljx.loadbalancer.Selector;
import com.ljx.transport.message.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性Hash负载均衡器
 * @Author LiuJixing
 * @Date 7/3/2024
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList);
    }
    private static class ConsistentHashSelector implements Selector {
        //hash环用来存放服务节点
        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();
        //虚拟节点的个数
        private static final int VIRTUAL_NODE_NUM = 256;
        private List<InetSocketAddress> serviceList;
        public ConsistentHashSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            //将节点转化为虚拟节点，并挂载到hash环上
            for(InetSocketAddress addr:serviceList){
                addNodeToCircle(addr);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            RpcRequest request = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(request.getRequestId());
            int hash = hash(requestId);
            //判断该hash值是否能直接落在一个服务器上
            if(!circle.containsKey(hash)){
                //获取大于该hash值的所有map
                SortedMap<Integer,InetSocketAddress> tailMap = circle.tailMap(hash);
                //如果没有大于该hash值的map，则返回第一个map
                hash = tailMap.isEmpty()?circle.firstKey():tailMap.firstKey();
                return circle.get(hash);
            } else {
                return circle.get(hash);
            }
        }

        /**
         * 将节点添加到hash环上
         * @param addr 节点地址
         */
        private void addNodeToCircle(InetSocketAddress addr) {
            //为节点生成匹配的虚拟节点
            for(int i=0;i<VIRTUAL_NODE_NUM;i++){
                //生成虚拟节点的hash值
                int hash = hash(addr.toString()+"-"+i);
                //将虚拟节点添加到hash环上
                circle.put(hash,addr);
                log.debug("add node to circle: {}-{}",addr,hash);
            }
        }
        private String toBinary(int i){
            String s = Integer.toBinaryString(i);
            int index = 32-s.length();
            StringBuilder sb = new StringBuilder();
            for(int j=0;j<index;j++){
                sb.append("0");
            }
            sb.append(s);
            return sb.toString();
        }
        /**
         * 从hash环上删除特定节点
         * @param addr 节点地址
         */
        private void removeNodeFromCircle(InetSocketAddress addr) {
            //为节点生成匹配的虚拟节点
            for(int i=0;i<VIRTUAL_NODE_NUM;i++){
                //生成虚拟节点的hash值
                int hash = hash(addr.toString()+"-"+i);
                //将虚拟节点添加到hash环上
                circle.remove(hash,addr);
            }
        }

        private int hash(String s) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(s.getBytes());
                byte[] digest = md5.digest();
                int h = 0;
                for (int i = 0; i < 4; i++) {
                    h <<= 8;
                    h |= ((int) digest[i]) & 0xFF;
                }
                return h;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 not supported", e);
            }
        }

        @Override
        public void reBalance() {
        }
    }
}
