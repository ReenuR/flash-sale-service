package com.flashsale.exception;

public class SoldOutException extends RuntimeException{
    public SoldOutException(String msg){
        super(msg);
    }
}
