package com.example.spring_security_demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequestDTO {
    
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;
}
