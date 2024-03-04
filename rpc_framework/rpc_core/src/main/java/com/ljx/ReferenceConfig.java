package com.ljx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    public void setInterface(Class<T> RpcClass) {
        this.interfaceRef = RpcClass;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //使用动态代理生成一个代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("hello proxy");
//                return method.invoke();
                return null;
            }
        });
        return (T) proxy;
    }
}
