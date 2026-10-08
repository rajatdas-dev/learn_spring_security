package com.example.spring_security_demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "user_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_subject",
                        columnNames = {
                                "provider","provider_subject"
                        }
                )
        }
)
public class UserIdentityEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user;
    
    @Column(nullable = false, length = 50)
    private String provider;
    
    @Column(
            name = "provider_subject",
            nullable = false,
            length = 255
    )
    private String providerSubject;
    
    @Column
    private String email;
    
    @Column(name = "email_verified")
    private boolean emailVerified;
}
