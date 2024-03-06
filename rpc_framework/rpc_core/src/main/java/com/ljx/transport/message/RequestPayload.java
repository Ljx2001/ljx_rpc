package com.ljx.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述请求调用方所请求的接口方法的描述
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    //请求的接口名 --HelloRpc
    private String interfaceName;
    //请求的方法名 --sayHi
    private String methodName;
    //请求的参数类型列表
    private Class<?>[] parameterTypes;
    //请求的参数值列表
    private Object[] parameterValues;
    //请求的返回值类型
    private Class<?> returnType;
}
