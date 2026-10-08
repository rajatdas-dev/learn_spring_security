package com.example.spring_security_demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private JwtService jwtService;
    
    private final OAuth2UserService oAuth2UserService;
    
    public OAuth2AuthenticationSuccessHandler(OAuth2UserService oAuth2UserService){
        this.oAuth2UserService = oAuth2UserService;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        UserDetails userDetails = oAuth2UserService.processOidcUser(oidcUser);
        
        String token = jwtService.generateToken(userDetails);
        
        response.sendRedirect(
                "/auth/oauth2/success?token" + token
        );
        
    }
}
