package com.example.spring_security_demo.service;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;
import com.example.spring_security_demo.dto.response.LoginResponseDTO;
import org.springframework.security.core.Authentication;

public interface AuthService {
    public void register(LoginRequestDTO loginRequestDTO);
    
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    
    public LoginResponseDTO refresh(String rawRefreshToken);
    
    public boolean logout(String refreshToken);
}
