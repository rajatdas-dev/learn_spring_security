package com.example.spring_security_demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OAuth2UserService {
    
    public UserDetails processOidcUser(OidcUser oidcUser);
}
