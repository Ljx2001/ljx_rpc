package com.ljx;

import com.ljx.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
@Component
@Slf4j
public class RpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("RPC服务提供方开始启动");
        RpcBootstrap.getInstance()
                .application("first-rpc-provider")
                //配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                //扫包批量发布
                .scan("com.ljx.impl")
                //启动服务
                .start();

    }
}
