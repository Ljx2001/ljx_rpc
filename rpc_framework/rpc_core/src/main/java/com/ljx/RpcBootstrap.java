package com.ljx;

import com.ljx.utils.NetUtils;
import com.ljx.utils.zookeeper.ZookeeperNode;
import com.ljx.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
public class RpcBootstrap {
    //private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);
    //RpcBootStrap是个单例，每个应用程序中只有一个实例
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();
    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;
    private ZooKeeper zooKeeper;

    private RpcBootstrap() {
        //构造启动引导程序时需要做一些什么初始化的事
    }

    /**
     * 获取RpcBootstrap实例
     *
     * @return RpcBootstrap实例
     */
    public static RpcBootstrap getInstance() {
        return rpcBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     *
     * @return this
     */
    public RpcBootstrap application(String appName) {
        //设置应用的名字
        this.applicationName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @return this
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {
        zooKeeper = ZookeeperUtil.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

    /**
     * 用来配置一个协议
     *
     * @return this
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     * ---------------------------------服务消费方相关的api---------------------------------
     */
    /**
     * 用来配置服务
     * @return this
     */
    public RpcBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }
    /**
     * ---------------------------------服务提供方相关的api---------------------------------
     */

    /**
     * 用来发布服务,将接口对应的实现类注册到注册中心
     * @param service 封装的需要发布的服务
     * @return this
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();
        //这个节点应该是一个持久节点
        if(!ZookeeperUtil.exists(parentNode,null,zooKeeper)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(parentNode, null), null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点,ip:port
        String localIp = NetUtils.getLocalIp();
        String localNode = parentNode + "/" + localIp + ":" + port;
        if(!ZookeeperUtil.exists(localNode,null,zooKeeper)){
            ZookeeperUtil.createNode(zooKeeper, new ZookeeperNode(localNode, null), null, CreateMode.EPHEMERAL);
        }

        if(log.isDebugEnabled()){
            log.debug("服务{}已经被注册", service.getInterface().getName());
        }
        return this;
    }
    /**
     * 用来批量发布服务
     * @param services 封装的需要发布的服务的列表
     * @return this
     */
    public RpcBootstrap publish(List<ServiceConfig<?>> services) {
        return this;
    }


    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtils.getLocalIp();
        System.out.println(ip);
    }
}

