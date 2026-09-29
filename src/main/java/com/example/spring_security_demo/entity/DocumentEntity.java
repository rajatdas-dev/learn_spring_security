package com.example.spring_security_demo.entity;

import com.example.spring_security_demo.service.DocumentClassification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class DocumentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    // We store the actual user id rather than trusting a username coming from the client
    @Column(nullable = false)
    private Long ownerUserId;
    
    // Resource ABAC attributes
    @Column(nullable = false)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentClassification classification;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void onCreate(){
        
        LocalDateTime now = LocalDateTime.now();
        
        createdAt = now;
        updatedAt = now;
    }
    
    @PrePersist
    public void onUpdate(){
        
        updatedAt = LocalDateTime.now();
    }
}
