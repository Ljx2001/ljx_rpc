package com.ljx.impl;

import com.ljx.HelloRpc;
import com.ljx.annotation.RpcService;

@RpcService
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer, I have received your message: " + msg + " from rpc server.";
    }
}
