package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserServiceImpl implements OAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails processOidcUser(OidcUser oidcUser) {
        
        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getName();
        String picture = oidcUser.getPicture();

        UserEntity user = userRepository
                .findByIdentityProviderAndProviderSubject(
                        "GOOGLE",
                        subject
                ).orElseGet(()-> createUser(
                        subject,
                        email,
                        name,
                        picture
                ));
        
        return User.withUsername(user.getUsername())
                .password("")
                .roles(user.getRole())
                .build();
    }
    
    
    private UserEntity createUser(
            String subject, 
            String email,
            String name,
            String picture
    ){
        
        UserEntity user = new UserEntity();
        
        user.setUsername(email);
        user.setEmail(email);
        user.setDisplayName(name);
        user.setProfilePictureUrl(picture);
        
        user.setIdentityProvider("GOOGLE");
        user.setProviderSubject(subject);
        
        user.setRole("USER");
        
        return userRepository.save(user);
    }
}
