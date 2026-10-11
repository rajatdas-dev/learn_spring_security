package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;
import com.example.spring_security_demo.dto.response.LoginResponseDTO;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.security.JwtService;
import com.example.spring_security_demo.service.AuthService;
import com.example.spring_security_demo.service.RefreshTokenRotation;
import com.example.spring_security_demo.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    
    private final RefreshTokenService refreshTokenService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    public AuthServiceImpl(RefreshTokenService refreshTokenService){
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public void register(LoginRequestDTO loginRequestDTO) {

        UserEntity userEntity = toUserEntity(loginRequestDTO);
        userRepository.save(userEntity);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
        );

//        UserEntity user = userRepository.findByUsername(loginRequestDTO.getUsername()).orElseThrow();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        
        UserEntity user = userRepository
                .findByUsername(principal.getUsername())
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USERNAME_NOT_FOUND,
                        "Authenticated user no longer exists"
                ));
        
//        String token = jwtService.generateToken(user);
        
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.create(user);
        
//        return new LoginResponseDTO(token);
        
        return tokenResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginResponseDTO refresh(String rawRefreshToken) {

        RefreshTokenRotation rotation = refreshTokenService.rotate(rawRefreshToken).orElse(null);
        
        if(rotation == null){
            return null;
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(rotation.user().getUsername());
        
        String accessToken = jwtService.generateToken(userDetails);
        return tokenResponse(accessToken,rotation.refreshToken());
    }

    @Transactional
    @Override
    public boolean logout(String refreshToken) {
        
        return refreshTokenService.revoke(refreshToken);
        
//        UserEntity user = userRepository.findByUsername(authentication.getName()).orElseThrow(()-> new UsernameNotFoundException(
//                "User name not found"
//        ));
//        
//        user.setTokenVersion(user.getTokenVersion() + 1);
//        
//        userRepository.save(user);
    }
    
    private LoginResponseDTO tokenResponse(
            String accessToken, 
            String refreshToken
    ){
        
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenTtlSeconds()
        );
    }

    public UserEntity toUserEntity(LoginRequestDTO loginRequestDTO){
        
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(loginRequestDTO.getUsername());
        userEntity.setPassword(passwordEncoder.encode(loginRequestDTO.getPassword()));
        
        // Never accept role from registration
        userEntity.setRole("USER");
        
        // Default ABAC department 
        // An Admin can later change this 
        
        userEntity.setDepartment("GENERAL");
        
        return userEntity;
    }
}
