package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 9/3/2024
 */
public class ResponseException extends RuntimeException {
    private final byte code;
    private String message;
    public ResponseException(byte code,String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
