package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
public class CircuitOpenException extends RuntimeException{
    public CircuitOpenException(String message) {
        super(message);
    }
}
