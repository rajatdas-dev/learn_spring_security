package com.example.spring_security_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public UserDetailsService userDetailsService(){
//
//        UserDetails user = User.builder()
//                .username("user")
//                .password("{noop}12345")
//                .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    // This tells Spring that When someone tries to authenticate, use my UserDetailsService to find the user 
    // and use BCrypt to verify the password
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ){

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        
        // Connecting BCrypt
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    
    // Authentication Manager 
    
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        
        return configuration.getAuthenticationManager();
    }
    
    // Security Filter Chain 
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws  Exception{
        
        httpSecurity.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Login/Register doesn't require authentication
                        .requestMatchers("/auth/**")
                        .permitAll()
                        
                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated());
        
        return httpSecurity.build();
    }
    

}
