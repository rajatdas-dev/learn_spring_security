package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.entity.RefreshTokenEntity;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.repository.RefreshTokenRepository;
import com.example.spring_security_demo.service.RefreshTokenRotation;
import com.example.spring_security_demo.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    private final RefreshTokenRepository repository;
    private final long refreshTokenTtlMs;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public RefreshTokenServiceImpl(
            RefreshTokenRepository repository,
            @Value("${jwt.refresh-token-ttl-ms:604800000}")
            long refreshTokenTtlMs
    ){
        this.repository = repository;
        
        if(refreshTokenTtlMs <= 0){
            throw new IllegalArgumentException(
                    "jwt.refresh-token-ttl-ms must be positive"
            );
        }
        
        this.refreshTokenTtlMs = refreshTokenTtlMs;
    }
    
    @Override
    @Transactional
    public String create(UserEntity user) {
        
        String familyId = UUID.randomUUID().toString();
        return createWithinFamily(user,familyId).rawToken;
    }

    /**
     * Validates and rotates a refresh token 
     * 
     * Invalid expired, or reused tokens return Optional.empty()
     * Reuse of a revoked token revokes active token in its family
     */

    @Override
    public Optional<RefreshTokenRotation> rotate(String rawToken) {
        
        if(rawToken == null || rawToken.isBlank()){
            return  Optional.empty();
        }

        Optional<RefreshTokenEntity> found = repository.findByTokenHashForUpdate(sha256(rawToken));
        
        if(found.isEmpty()){
            return Optional.empty();
        }
        
        RefreshTokenEntity current = found.get();
        Instant now = Instant.now();
        
        // A previously revoked token is being reused 
        if(current.getRevokedAt() != null){
            repository.revokeActiveFamily(current.getFamilyId(), now);
            
            return Optional.empty();
        }
        
        // Expired refresh token 
        if(!current.getExpiresAt().isAfter(now)){
            current.setRevokedAt(now);
            repository.save(current);
            return Optional.empty();
        }
        
        // Invalidate the old token
        current.setRevokedAt(now);
        
        // Generate the replacement in the same session family 
        String nextRawToken = createWithinFamily(
                current.getUser(),
                current.getFamilyId()
        ).rawToken;
        
        current.setReplacedByHash(sha256(nextRawToken));
        repository.save(current);
        
        return Optional.of(
                new RefreshTokenRotation(
                        current.getUser(),
                        nextRawToken
                )
        );
    }

    /**
     * Revokes the session family associated with this refresh token 
     */

    @Override
    @Transactional
    public boolean revoke(String rawToken) {
        if(rawToken == null || rawToken.isBlank()){
            return false;
        }
        
        Optional<RefreshTokenEntity> found = repository.findByTokenHashForUpdate(sha256(rawToken));
        
        if(found.isEmpty()){
            return false;
        }
        
        RefreshTokenEntity token = found.get();
        
        int changed = repository.revokeActiveFamily(
                token.getFamilyId(),
                Instant.now()
        );
        
        return changed >0 || token.getRevokedAt() != null;
    }
    
    private CreatedToken createWithinFamily(
            UserEntity user,
            String familyId
    ){
        
        byte[] random = new byte[64];
        secureRandom.nextBytes(random);
        
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(random);

        Instant now = Instant.now();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setTokenHash(sha256(rawToken));
        entity.setFamilyId(familyId);
        entity.setUser(user);
        entity.setCreatedAt(now);
        entity.setExpiresAt(now.plusMillis(refreshTokenTtlMs));
        
        repository.save(entity);
        
        return new CreatedToken(rawToken);
    }
    
    private String sha256(String value){
        try{
            
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception){
            
            throw new IllegalArgumentException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
    
    private record CreatedToken(String rawToken){}
}
