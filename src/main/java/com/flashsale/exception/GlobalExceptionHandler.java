package com.flashsale.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitException(RateLimitExceededException e){
        return ResponseEntity.status(429).body(e.getMessage());
    }

    @ExceptionHandler(SoldOutException.class)
    public ResponseEntity<String> handleSoldOutException(SoldOutException e){
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e){
        e.printStackTrace();
        return ResponseEntity.status(500).body("Something went wrong");
    }
}
