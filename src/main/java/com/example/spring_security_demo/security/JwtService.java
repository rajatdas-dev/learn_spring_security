package com.example.spring_security_demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Service
public class JwtService {
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    public JwtService(PrivateKey privateKey, PublicKey publicKey){
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    
    public String generateToken(UserDetails userDetails){
        
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + 1000 * 60 * 60
                        )
                )
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }
    
    public Claims extractClaims(String token){
        
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

//    private final String secretKey;
//    private final long expiration;
//    
////    private final String secretKey = "my/9RrRUmI9nCOBio9V7stiuUKncs6yLJRQs0rGwgsU=";
//
//    public JwtService(
//            @Value("${jwt.secret}") String secretKey,
//            @Value("${jwt.expiration}") long expiration
//    ) {
//        this.secretKey = secretKey;
//        this.expiration = expiration;
//    }
//    
//    public String generateToken(UserDetails userDetails){
//
////        SecretKey key = Jwts.SIG.HS256.key().build();
////
////        String secret = Encoders.BASE64.encode(key.getEncoded());
////
////        System.out.println(secret);
//        
//        return Jwts.builder()
//                .subject(userDetails.getUsername())
//                .issuedAt(new Date())
//                .expiration(
//                        new Date(
//                                System.currentTimeMillis() + 1000 * 60 * 60
//                        )
//                )
//                .signWith(getSigningKey())
//                .compact();
//    }
//    
//    private SecretKey getSigningKey(){
//        
//        return Keys.hmacShaKeyFor(
//                Decoders.BASE64.decode(secretKey)
//        );
//    }
}
