package com.demo.demoautovalidation.validation.exception;

public class ConcurrentRequestException extends RuntimeException {

    public ConcurrentRequestException(String message) {
        super(message);
    }
}
