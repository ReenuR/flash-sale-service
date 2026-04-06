package com.flashsale.exception;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(String msg){
        super(msg);
    }
}
