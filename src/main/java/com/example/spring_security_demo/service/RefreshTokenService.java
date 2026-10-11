package com.example.spring_security_demo.service;

import com.example.spring_security_demo.entity.RefreshTokenEntity;
import com.example.spring_security_demo.entity.UserEntity;

import java.util.Optional;

public interface RefreshTokenService {
    
    public String create(UserEntity user);
    
    public Optional<RefreshTokenRotation> rotate(String rawToken);
    
    public boolean revoke(String rawToken);
    
    
}
