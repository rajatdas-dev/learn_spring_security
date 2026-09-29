package com.example.spring_security_demo.authorization;

import com.example.spring_security_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("userAccessPolicy")
public class UserAccessPolicy {
    
    @Autowired
    private UserRepository userRepository;
}
