package com.example.spring_security_demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PKCS12Attribute;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {
    
    // ECDSA 
    
    @Bean
    public PrivateKey privateKey() throws Exception{
        
        String key = Files.readString(
                Path.of("src/main/resources/keys/private_key.pem")
        );
        
        key = key.
                replace("-----BEGIN PRIVATE KEY-----","")
                .replace("-----END PRIVATE KEY-----","")
                .replaceAll("\\s","");
        
        byte[] decoded = Base64.getDecoder().decode(key);
        
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        
        return keyFactory.generatePrivate(keySpec);
    }
    
    @Bean
    public PublicKey publicKey() throws Exception {
        
        String key = Files.readString(
                Path.of("src/main/resources/keys/public_key.pem")
        );
        
        key = key
                .replace("-----BEGIN PUBLIC KEY-----","")
                .replace("-----END PUBLIC KEY-----","")
                .replaceAll("\\s","");
        
        byte[] decoded = Base64.getDecoder().decode(key);
        
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        
        return keyFactory.generatePublic(keySpec);
    }

    
    // RSA 

//    @Value("${jwt.private-key}")
//    private Resource privateKeyResource;
//
//    @Value("${jwt.public-key}")
//    private Resource publicKeyResource;
//
//
//    @Bean
//    public PrivateKey privateKey() throws Exception {
//
//        String key = readKey(privateKeyResource);
//
//        byte[] keyBytes = Base64.getDecoder().decode(key);
//
//        PKCS8EncodedKeySpec keySpec =
//                new PKCS8EncodedKeySpec(keyBytes);
//
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//
//        return keyFactory.generatePrivate(keySpec);
//    }
//
//    @Bean
//    public PublicKey publicKey() throws Exception {
//
//        String key = readKey(publicKeyResource);
//
//        byte[] keyBytes = Base64.getDecoder().decode(key);
//
//        X509EncodedKeySpec keySpec =
//                new X509EncodedKeySpec(keyBytes);
//
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//
//        return keyFactory.generatePublic(keySpec);
//    }
//
//    private String readKey(Resource resource) throws IOException {
//
//        String key = new String(
//                resource.getInputStream().readAllBytes(),
//                StandardCharsets.UTF_8
//        );
//
//        return key
//                .replace("-----BEGIN PRIVATE KEY-----", "")
//                .replace("-----END PRIVATE KEY-----", "")
//                .replace("-----BEGIN PUBLIC KEY-----", "")
//                .replace("-----END PUBLIC KEY-----", "")
//                .replaceAll("\\s+", "");
//    }
}
