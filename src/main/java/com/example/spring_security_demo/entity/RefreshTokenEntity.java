package com.example.spring_security_demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_family", columnList = "family_id"),
                @Index(name = "idx_refresh_token_user",columnList = "user_id")
        }
)
@NoArgsConstructor
public class RefreshTokenEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    
    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(name = "created_at",nullable = false)
    private Instant createdAt;
    
    @Column(name = "expires_at",nullable = false)
    private Instant expiresAt;
    
    @Column(name = "revoked_at")
    private Instant revokedAt;
    
    @Column(name = "replaced_by_hash",length = 64)
    private String replacedByHash;
}
