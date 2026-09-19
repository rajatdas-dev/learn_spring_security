package com.example.spring_security_demo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppErrorResponse {
    
    private boolean success;
    private int status;
    private ErrorCode errorCode;
    private String message;
    private String path;
    private LocalDateTime timeStamp;
    private String traceId;
}
