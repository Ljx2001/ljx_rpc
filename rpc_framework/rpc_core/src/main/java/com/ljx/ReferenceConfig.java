package com.ljx;

import com.ljx.discovery.Registry;
import com.ljx.proxy.handler.RpcConsumerInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    public void setInterface(Class<T> RpcClass) {
        this.interfaceRef = RpcClass;
    }

    /**
     * 代理设计模式，生成一个api的代理对象
     * @return 代理对象
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(interfaceRef, registry);
        //使用动态代理生成一个代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
        return (T) proxy;
    }
}
