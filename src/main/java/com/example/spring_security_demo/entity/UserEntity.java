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
@Table(name = "users")
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
    
    // ABAC subject attributes
    @Column
    private String department;
    
    // Safe profile attribute 
    @Column
    private String displayName;
    
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
