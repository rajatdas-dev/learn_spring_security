package com.example.spring_security_demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}
