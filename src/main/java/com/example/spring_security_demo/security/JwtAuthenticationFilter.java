package com.example.spring_security_demo.security;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        try{

            Claims claims = jwtService.extractClaims(token);
            String username = claims.getSubject();
            
            Long userId = claims.get("uid",Long.class);
            
            Long tokenVersion = claims.get("ver",Long.class);
            
            if(username == null || userId == null || tokenVersion == null){
                filterChain.doFilter(
                        request,response
                );
                return;
            }   
            
            if(SecurityContextHolder.getContext().getAuthentication() == null){

                UserEntity user = userRepository.findById(userId).orElse(null);
                
                if(user == null || !user.getUsername().equals(username) || !user.getTokenVersion().equals(tokenVersion)){
                    filterChain.doFilter(
                            request,
                            response
                    );
                    return;
                }
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            
//            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
//                
//                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                        userDetails,
//                        null,
//                        userDetails.getAuthorities()
//                );
//                
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            }
        } catch (JwtException| IllegalArgumentException e){
            
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request,response);
    }
}
