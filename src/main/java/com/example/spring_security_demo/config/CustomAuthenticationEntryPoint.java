package com.example.spring_security_demo.config;

import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.response.AppErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        log.error(
                "401 UNAUTHORIZED | method={} | uri={} | message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//        ApiResponse<Void> apiResponse = new ApiResponse<>(
//                false,
//                "Authentication required",
//                null
//        );

        String traceId = MDC.get("traceId");
        AppErrorResponse appErrorResponse = new AppErrorResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED,
                "Authentication Required",
                request.getRequestURI(),
                LocalDateTime.now(),
                traceId
                
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(appErrorResponse)
        );
    }
}