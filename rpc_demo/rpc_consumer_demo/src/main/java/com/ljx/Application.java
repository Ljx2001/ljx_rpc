package com.ljx;


/**
 * @Author LiuJixing
 * @Date 1/3/2024
 */
public class Application {
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
                .reference(reference);
        //获取一个代理对象
        HelloRpc helloRpc = reference.get();
        //调用代理对象的方法
        String result = helloRpc.sayHi("rpc");
    }
}
