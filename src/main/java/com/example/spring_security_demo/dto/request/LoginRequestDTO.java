package com.example.spring_security_demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

//    @Column(nullable = false)
    @NotNull(message = "username is required")
    private String username;

    @NotNull(message = "password is required")
    private String password;
}
