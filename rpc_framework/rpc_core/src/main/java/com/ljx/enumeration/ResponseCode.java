package com.ljx.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public enum ResponseCode {
    SUCCESS((byte)1, "调用成功"),FAIL((byte)2, "调用失败");
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
