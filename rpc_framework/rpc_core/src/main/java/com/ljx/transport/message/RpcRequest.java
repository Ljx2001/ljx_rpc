package com.ljx.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务调用方发起的请求内容
 * @Author LiuJixing
 * @Date 5/3/2024
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    //请求的id
    private long requestId;
    //请求的类型
    private byte requestType;
    //压缩的方式
    private byte compressType;
    //序列化的方式
    private byte serializeType;
    //具体的消息体
    private RequestPayload requestPayload;
}
