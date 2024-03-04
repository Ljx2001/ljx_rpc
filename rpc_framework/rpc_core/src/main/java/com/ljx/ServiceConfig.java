package com.ljx;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
public class ServiceConfig<T> {
    private Class<T> interfaceProvider;
    private Object ref;
    public void setInterface(Class<T> helloRpcClass) {
        this.interfaceProvider = helloRpcClass;
    }

    public void setRef(T helloRpc) {
        this.ref = helloRpc;
    }

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }
}
