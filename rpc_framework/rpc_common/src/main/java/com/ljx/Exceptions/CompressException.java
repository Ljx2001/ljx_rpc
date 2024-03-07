package com.ljx.Exceptions;

/**
 * @Author LiuJixing
 * @Date 6/3/2024
 */
public class CompressException extends RuntimeException{
    public CompressException(){

    }
    public CompressException(String message){

        super(message);
    }
    public CompressException(Throwable cause){
        super(cause);
    }
}
