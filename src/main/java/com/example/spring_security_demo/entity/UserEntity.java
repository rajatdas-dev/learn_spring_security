package com.example.spring_security_demo.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "users",
uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_subject",
                        columnNames = {"identity_provider","provider_subject"}
                )
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role;
    
    @Column(name = "identity_provider")
    private String identityProvider;
    
    @Column(name = "provider_subject")
    private String providerSubject;
    
    @Column
    private String email;
    
    // ABAC subject attributes
    @Column
    private String department;
    
    // Safe profile attribute 
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @PrePersist
    public void applyDefault(){
        
        if(role == null || role.isBlank()){
            role = "USER";
        }
        
        if(department == null || department.isBlank()){
            department = "GENERAL";
        }
    }

}
