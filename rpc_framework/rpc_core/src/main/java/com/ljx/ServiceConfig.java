package com.ljx;

/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
public class ServiceConfig<T> {
    private Class<?> interfaceProvider;
    private Object ref;
    private String group;
    public void setInterface(Class<?> helloRpcClass) {
        this.interfaceProvider = helloRpcClass;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
