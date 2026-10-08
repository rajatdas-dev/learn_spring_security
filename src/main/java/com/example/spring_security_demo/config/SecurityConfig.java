package com.example.spring_security_demo.config;

import com.example.spring_security_demo.security.CustomAccessDeniedHandler;
import com.example.spring_security_demo.security.JwtAuthenticationFilter;
import com.example.spring_security_demo.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
//    private final UserDetailsService userDetailsService;
//
//    public SecurityConfig(UserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }

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
        
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
//        return SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
//        return new BCryptPasswordEncoder();
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
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        
        return configuration.getAuthenticationManager();
    }
    
    // Security Filter Chain 
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            CustomAccessDeniedHandler accessDeniedHandler,
            AuthenticationEntryPoint authenticationEntryPoint,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler
    ) throws  Exception{
        
       return httpSecurity.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Login/Register doesn't require authentication
                        .requestMatchers("/auth/**",
                                "/oauth2/**",
                                "/login/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated())
               .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2AuthenticationSuccessHandler))
        
               .exceptionHandling(exception -> exception
                       .accessDeniedHandler(accessDeniedHandler)
                       .authenticationEntryPoint(authenticationEntryPoint))
               
               .sessionManagement(session -> session.sessionCreationPolicy(
                       SessionCreationPolicy.STATELESS
               ))
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                ).build();
    }
    

}
