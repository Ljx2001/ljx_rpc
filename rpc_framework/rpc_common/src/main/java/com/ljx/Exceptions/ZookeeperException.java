package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
public class ZookeeperException extends RuntimeException{
    public ZookeeperException(){

    }
    public ZookeeperException(String message){

        super(message);
    }
    public ZookeeperException(Throwable cause){
        super(cause);
    }
}
