package com.example.spring_security_demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    
    private final String secretKey = "my/9RrRUmI9nCOBio9V7stiuUKncs6yLJRQs0rGwgsU=";
    
    public String generateToken(UserDetails userDetails){

//        SecretKey key = Jwts.SIG.HS256.key().build();
//
//        String secret = Encoders.BASE64.encode(key.getEncoded());
//
//        System.out.println(secret);
        
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + 1000 * 60 * 60
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }
    
    private SecretKey getSigningKey(){
        
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }
}
