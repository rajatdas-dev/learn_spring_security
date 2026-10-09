package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.security.JwtService;
import com.example.spring_security_demo.service.OIdcIdentityService;
import com.example.spring_security_demo.service.OidcAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class OidcAuthenticationServiceImpl implements OidcAuthenticationService {
    
    @Autowired
    private OIdcIdentityService oIdcIdentityService;
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    public String authenticateGoogle(Jwt idToken) {
        UserEntity user = oIdcIdentityService.authenticateGoogle(idToken);
        
        return jwtService.generateToken(user);
    }
}
