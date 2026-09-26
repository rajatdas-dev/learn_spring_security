package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;
import com.example.spring_security_demo.dto.response.LoginResponseDTO;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.security.JwtService;
import com.example.spring_security_demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Override
    public void register(LoginRequestDTO loginRequestDTO) {

        UserEntity userEntity = toUserEntity(loginRequestDTO);
        userRepository.save(userEntity);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        String token = jwtService.generateToken(userDetails);
        
        return new LoginResponseDTO(token);
    }

    public UserEntity toUserEntity(LoginRequestDTO loginRequestDTO){
        
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(loginRequestDTO.getUsername());
        userEntity.setPassword(passwordEncoder.encode(loginRequestDTO.getPassword()));
        userEntity.setRole("USER");
        return userEntity;
    }
}
