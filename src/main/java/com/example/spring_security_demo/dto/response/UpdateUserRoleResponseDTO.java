package com.example.spring_security_demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoleResponseDTO {
    
    private Long userId;
    private String message;
    private String userRole;
}
