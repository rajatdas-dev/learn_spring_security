package com.example.spring_security_demo.service;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;

public interface AuthService {
    public void register(LoginRequestDTO loginRequestDTO);
}
