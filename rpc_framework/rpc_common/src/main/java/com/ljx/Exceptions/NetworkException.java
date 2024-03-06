package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 4/3/2024
 */
public class NetworkException extends RuntimeException{
    public NetworkException(){

    }
    public NetworkException(String message){

        super(message);
    }
    public NetworkException(Throwable cause){
        super(cause);
    }

}
