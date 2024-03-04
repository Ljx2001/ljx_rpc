package com.ljx;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
public class Constant {
    //默认的zookeeper的连接地址
    public static final String DEFAULT_ZK_ADDRESS = "127.0.0.1:2181";
    //默认的zoopkeeper的连接超时时间
    public static final int DEFAULT_ZK_SESSION_TIMEOUT = 10000;
    //默认的根节点
    public static final String BASE_PATH = "/rpc-metadata";
    //默认的服务提供者节点
    public static final String BASE_PROVIDER_PATH= BASE_PATH+"/provider";
    //默认的服务消费者节点
    public static final String BASE_CONSUMER_PATH = BASE_PATH+"/consumer";

}
