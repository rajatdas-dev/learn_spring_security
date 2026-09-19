package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.AuthService;
import com.example.spring_security_demo.util.mapper.ModelMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ModelMapperUtil modelMapperUtil;

    @Override
    public void register(LoginRequestDTO loginRequestDTO) {

        UserEntity userEntity = toUserEntity(loginRequestDTO);
        userRepository.save(userEntity);
    }
    
    public UserEntity toUserEntity(LoginRequestDTO loginRequestDTO){
        
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(loginRequestDTO.getUsername());
        userEntity.setPassword(passwordEncoder.encode(loginRequestDTO.getPassword()));
        return userEntity;
    }
}
