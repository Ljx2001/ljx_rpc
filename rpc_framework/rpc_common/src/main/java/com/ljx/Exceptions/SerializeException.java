package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public class SerializeException extends RuntimeException{
    public SerializeException(){

    }
    public SerializeException(String message){

        super(message);
    }
    public SerializeException(Throwable cause){
        super(cause);
    }
}
