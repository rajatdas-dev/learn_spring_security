package com.example.spring_security_demo.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface OidcAuthenticationService {
    
    public String authenticateGoogle(Jwt idToken);
}
