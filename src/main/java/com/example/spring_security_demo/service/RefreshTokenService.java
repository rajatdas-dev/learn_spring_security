package com.example.spring_security_demo.service;

import com.example.spring_security_demo.entity.RefreshTokenEntity;
import com.example.spring_security_demo.entity.UserEntity;

import java.util.Optional;

public interface RefreshTokenService {
    
    String create(UserEntity user);
    
    Optional<RefreshTokenRotation> rotate(String rawToken);
    
    boolean revoke(String rawToken);
}
