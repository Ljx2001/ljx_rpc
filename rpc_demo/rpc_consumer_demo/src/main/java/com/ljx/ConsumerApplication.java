package com.ljx;


import com.ljx.core.HeartbeatDetector;
import com.ljx.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //想办法获取代理对象,使用ReferenceConfig进行封装
        //reference中一定有生成代理的模板方法
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        //设置接口
        reference.setInterface(HelloRpc.class);
        //代理对象需要 1.连接注册中心 2.拉取服务列 3.选择一个服务然后建立连接 4.发送请求，携带一些信息（接口名，方法名，参数列表），获得结果
        RpcBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        //获取一个代理对象
        HelloRpc helloRpc = reference.get();
//        for (int i = 0; i < 200; i++) {
//            //调用代理对象的方法
//            String result = helloRpc.sayHi("你好");
//            log.debug("调用结果：{}", result);
//        }
        //睡一会
        while(true){
            for (int i = 0; i < 20; i++) {
                //调用代理对象的方法
                String result = helloRpc.sayHi("你好");
                log.debug("调用结果：{}", result);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        //调用代理对象的方法
//        String result = helloRpc.sayHi("你好");
//        log.info("调用结果：{}", result);
    }
}
