package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.dto.request.LoginRequestDTO;
import com.example.spring_security_demo.dto.request.OidcLoginRequestDTO;
import com.example.spring_security_demo.dto.response.LoginResponseDTO;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.security.JwtService;
import com.example.spring_security_demo.service.AuthService;
import com.example.spring_security_demo.service.OidcAuthenticationService;
import com.example.spring_security_demo.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
//    @Autowired
//    private AuthenticationManager authenticationManager;
//    
//    @Autowired
//    private JwtService jwtService;
//    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtDecoder googleOidcJwtDecoder;
    
    @Autowired
    private OidcAuthenticationService oidcAuthenticationService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid
            @RequestBody LoginRequestDTO loginRequestDTO
    ){
     
        authService.register(loginRequestDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Registration Successfull"
                )
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO loginRequestDTO
            ){
        
        LoginResponseDTO loginResponseDTO = authService.login(loginRequestDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login Successful !",
                        loginResponseDTO
                )
        );
    }
    
    @PostMapping("/oidc/google")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> googleLogin(
            @Valid @RequestBody OidcLoginRequestDTO requestDTO
            ){

        Jwt idToken = googleOidcJwtDecoder.decode(
                requestDTO.getIdToken()
        );
        
        String token = oidcAuthenticationService.authenticateGoogle(idToken);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Google Oidc login successful",
                        new LoginResponseDTO(token)
                )
        );
    }
}
