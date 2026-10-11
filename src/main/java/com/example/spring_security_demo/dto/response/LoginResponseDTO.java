package com.example.spring_security_demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
