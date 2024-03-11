package com.ljx.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成功码 20(方法成功调用) 21(心跳成功返回)
 * 错误码(服务端) 50(请求的方法不存在)
 * 错误码(客户端) 44
 * 负载码 31(服务器负载过高被限流)
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public enum ResponseCode {
    SUCCESS((byte)20, "调用成功返回"),
    SUCCESS_HEARTBEAT((byte)21, "心跳成功返回"),
    RATE_LIMIT((byte)31, "服务器负载过高被限流"),
    RESOURCE_NOT_FOUND((byte)44, "请求的方法不存在"),
    FAIL((byte)50, "调用方法发生异常"),
    BECLOSEING((byte)51, "服务端关闭连接");

    private byte code;
    private String message;
    ResponseCode(byte code, String message) {
        this.code = code;
        this.message = message;
    }
    public byte getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
