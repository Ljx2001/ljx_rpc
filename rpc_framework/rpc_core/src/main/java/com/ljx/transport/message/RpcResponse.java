package com.ljx.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应
 * @Author LiuJixing
 * @Date 6/3/2024
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse {
    //请求的id
    private long requestId;
    //压缩的方式
    private byte compressType;
    //序列化的方式
    private byte serializeType;
    //响应码 1-成功 2-异常
    private byte code;
    //具体的消息体
    private Object body;
    //时间戳
    private long timestamp;
}
