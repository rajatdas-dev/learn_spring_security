package com.example.spring_security_demo.security;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final UserRepository userRepository;
    
    private final long accessTokenTtlMs;
    private final long clockSkewSeconds;
    
    private final String issuer;
    private final String audience;
    
    public JwtService(
            PrivateKey privateKey,
            PublicKey publicKey,
            UserRepository userRepository,
            @Value("${jwt.access-token-ttl-ms: 900000}")
            long accessTokenTtlMs,
            @Value("${jwt.clock-skew-seconds:60}")
            long clockSkewSeconds,
            @Value("${jwt.issuer")
            String issuer,
            @Value("${jwt.audience}")
            String audience
    ){

        if(accessTokenTtlMs <= 0 || clockSkewSeconds<0){
            throw new IllegalArgumentException(
                    "Invalid JWT time configuration"
            );
        }
        
        
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.userRepository = userRepository;
        
        
//        if(accessTokenTtlMs <= 0){
//            throw new IllegalArgumentException(
//                    "jwt.access-token-ttl-ms must be positive"
//            );
//        }
        
        this.accessTokenTtlMs = accessTokenTtlMs;
        this.clockSkewSeconds = clockSkewSeconds;
        
        this.issuer = issuer;
        this.audience = audience;
    }
    
    public String generateToken(UserEntity user){
        
        Instant now = Instant.now();
        
        long tokenVersion = user.getTokenVersion() == null
                ?0L 
                : user.getTokenVersion();
        
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .claim("aud",audience)
                .claim("uid",user.getId())
                .claim("role",user.getRole())
                .claim("ver",tokenVersion)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
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
    
//    public Claims extractClaims(String token){
//        
//        return Jwts.parser()
//                .verifyWith(publicKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
    
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

public Claims extractClaims(String token) {

    Claims claims = Jwts.parser()

            .verifyWith(publicKey)

            .clockSkewSeconds(clockSkewSeconds)

            .build()

            .parseSignedClaims(token)

            .getPayload();

    if (!issuer.equals(claims.getIssuer())) {
        throw new JwtException("Invalid JWT issuer");
    }

    Object audienceClaim = claims.get("aud");

    if (!(audienceClaim instanceof String value)
            || !audience.equals(value)) {

        throw new JwtException("Invalid JWT audience");
    }

    if (claims.getSubject() == null
            || claims.getId() == null
            || claims.getIssuedAt() == null
            || claims.getNotBefore() == null
            || claims.getExpiration() == null
            || claims.get("uid") == null
            || claims.get("ver") == null) {

        throw new JwtException(
                "Required JWT claim is missing"
        );
    }

    if (claims.getIssuedAt()
            .toInstant()
            .isAfter(
                    Instant.now().plusSeconds(clockSkewSeconds)
            )) {

        throw new JwtException(
                "JWT issued-at time is in the future"
        );
    }

    return claims;
}

//    public long getAccessTokenTtlSeconds() {
//        return accessTokenTtlMs / 1000;
//    }
}
