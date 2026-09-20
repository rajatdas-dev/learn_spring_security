package com.example.spring_security_demo.exception;

import com.example.spring_security_demo.response.AppErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AppErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                exception.getStatus(),
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }
    
    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<AppErrorResponse> handleInvalidCredential(
            InvalidCredentialException exception,
            HttpServletRequest request
    ){
        return buildResponse(
                exception.getStatus(),
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }
    
    
    
    
    private ResponseEntity<AppErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ){
        String traceId = MDC.get("traceId");    
        AppErrorResponse response = new AppErrorResponse(
                false,
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                traceId
        );
        
        return ResponseEntity.status(status).body(response);
    }
    
    
}
