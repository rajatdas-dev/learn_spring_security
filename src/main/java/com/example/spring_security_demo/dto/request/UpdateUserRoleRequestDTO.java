package com.example.spring_security_demo.dto.request;

import com.example.spring_security_demo.service.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoleRequestDTO {
    
    @NotNull(message = "User id is required !!")
    private Long userId;
    
    @NotNull(message = "User role is required !!")
    private UserRole userRole;
}
