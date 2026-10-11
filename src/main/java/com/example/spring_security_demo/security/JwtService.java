package com.example.spring_security_demo.security;

import com.example.spring_security_demo.entity.UserEntity;
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
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    private final long accessTokenTtlMs;
    
    public JwtService(
            PrivateKey privateKey, PublicKey publicKey,
            @Value("${jwt.access-token-ttl-ms: 900000}")
            long accessTokenTtlMs
    ){
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        
        if(accessTokenTtlMs <= 0){
            throw new IllegalArgumentException(
                    "jwt.access-token-ttl-ms must be positive"
            );
        }
        
        this.accessTokenTtlMs = accessTokenTtlMs;
    }
    
    public String generateToken(UserEntity user){
        
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid",user.getId())
                .claim("role",user.getRole())
                .claim("ver",user.getTokenVersion())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis() + 1000 * 60 * 60
                        )
                )
                .signWith(privateKey, Jwts.SIG.ES256)   // for ECDSA
//                .signWith(privateKey, Jwts.SIG.RS256)   // for RSA
                .compact();
    }

    public String generateToken(
            UserDetails userDetails
    ) {

        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
//                .issuedAt(new Date())
//                .expiration(
//                        new Date(
//                                System.currentTimeMillis()
//                                        + 1000L * 60 * 60
//                        )
//                )
                
                .expiration(
                        Date.from(now.plusMillis(accessTokenTtlMs))
                )
                .signWith(
                        privateKey,
                        Jwts.SIG.ES256
                )
                .compact();
    }
    
    public Claims extractClaims(String token){
        
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public long getAccessTokenTtlSeconds(){
        return accessTokenTtlMs / 1000;
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
