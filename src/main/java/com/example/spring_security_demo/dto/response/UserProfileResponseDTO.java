package com.example.spring_security_demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDTO {
    
    private Long userId;
    private String username;
    private String displayName;
    private String role;
    private String department;
}

