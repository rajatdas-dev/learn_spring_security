package com.example.spring_security_demo.service;

import com.example.spring_security_demo.dto.request.CreateDocumentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateDocumentRequestDTO;
import com.example.spring_security_demo.dto.response.DocumentResponseDTO;

import java.util.List;

public interface DocumentService {
    
    DocumentResponseDTO createDocument(String username, CreateDocumentRequestDTO requestDTO);
    
    List<DocumentResponseDTO> getMyDocuments(String username);
    
    DocumentResponseDTO getDocument(Long documentId);
    
    DocumentResponseDTO updateDocument(Long documentId, UpdateDocumentRequestDTO requestDTO);
    
    void deleteDocument(Long documentId);
}
