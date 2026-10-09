package com.example.spring_security_demo.service;

import com.example.spring_security_demo.entity.UserEntity;
import org.springframework.security.oauth2.jwt.Jwt;

public interface OIdcIdentityService {
    
    public UserEntity authenticateGoogle(Jwt idToken);
}
