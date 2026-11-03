package com.example.spring_security_demo.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends BusinessException{
    
    public UnAuthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.UNAUTHORIZED, message);
    }
}
