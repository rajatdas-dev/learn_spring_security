package com.example.spring_security_demo.security;

import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.response.AppErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        log.error(
                "403 FORBIDDEN | method={} | uri={} | message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//        ApiResponse<Void> apiResponse = new ApiResponse<>(
//                false,
//                "You do not have permission to access this resource",
//                null
        
//        );

        String traceId = MDC.get("traceId");
        AppErrorResponse appErrorResponse = new AppErrorResponse(
                false,
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.FORBIDDEN,
                "You don't have permission to access this resource",
                request.getRequestURI(),
                LocalDateTime.now(),
                traceId
                
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(appErrorResponse)
        );
    }
}