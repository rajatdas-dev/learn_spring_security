package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.entity.UserIdentityEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.UserIdentityRepository;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.OIdcIdentityService;
import com.example.spring_security_demo.service.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class OidcIdentityServiceImpl implements OIdcIdentityService {
    
    private static final String GOOGLE = "GOOGLE";
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserIdentityRepository userIdentityRepository;
    
    @Override
    @Transactional
    public UserEntity authenticateGoogle(Jwt idToken) {
        
        String subject = idToken.getSubject();
        
        String email = idToken.getClaimAsString("email");
        
        String name = idToken.getClaimAsString("name");
        
        String picture = idToken.getClaimAsString("picture");
        
        Boolean emailVerified = idToken.getClaimAsBoolean("email_verified");
        
        if(subject == null || subject.isBlank()){
            throw new ResourceNotFoundException(
                    ErrorCode.MISSING,
                    "OIDC is missing");
        }
        
        if(email == null || email.isBlank()){
            throw new ResourceNotFoundException(
                    ErrorCode.MISSING,
                    "Email is missing from OIDC  identity"
            );
        }
        
        if(!Boolean.TRUE.equals(emailVerified)){
            throw new ResourceNotFoundException(
                    ErrorCode.NOT_VERIFIED,
                    "Google Email is not verified"
            );
        }
        
        return userIdentityRepository.findByProviderAndProviderSubject(
                GOOGLE,
                subject
        ).map(UserIdentityEntity::getUser)
                .orElseGet(()-> createOrLinkGoogleUser(
                        subject,
                        email,
                        name,
                        picture
                ));
        
    }
    
    private UserEntity createOrLinkGoogleUser(
            String subject,
            String email,
            String name,
            String picture
    ){
        UserEntity user = userRepository.findByUsername(email)
                .orElseGet(()-> {
                    UserEntity newUser = new UserEntity();
                    
                    newUser.setUsername(email);
                    newUser.setPassword(null);
                    newUser.setRole(UserRole.USER.name());
                    newUser.setEmail(email);
                    newUser.setDisplayName(name);
                    newUser.setProfilePictureUrl(picture);
                    newUser.setTokenVersion(0L);
                    return  userRepository.save(newUser);
                });
        
        UserIdentityEntity identityEntity = new UserIdentityEntity();
        
        identityEntity.setUser(user);
        identityEntity.setProvider(GOOGLE);
        identityEntity.setProviderSubject(subject);
        identityEntity.setEmail(email);
        identityEntity.setEmailVerified(true);
        userIdentityRepository.save(identityEntity);
        
        return user;
    }
}
