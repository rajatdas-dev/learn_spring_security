package com.example.spring_security_demo.service;

import com.example.spring_security_demo.entity.UserEntity;

public record RefreshTokenRotation (
        UserEntity user,
        String refreshToken
){}
