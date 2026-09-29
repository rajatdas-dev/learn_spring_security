package com.example.spring_security_demo.dto.response;

import com.example.spring_security_demo.service.DocumentClassification;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@NotBlank
@AllArgsConstructor
public class DocumentResponseDTO {
    
    private Long id;
    private String title;
    private String content;
    private Long ownerUserId;
    private String ownerUsername;
    private String department;
    private DocumentClassification classification;
    private LocalDateTime createdAt;    
    private LocalDateTime updatedAt;    
}
