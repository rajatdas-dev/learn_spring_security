package com.example.spring_security_demo.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException  extends BusinessException{

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.NOT_FOUND, message);
    }
}
