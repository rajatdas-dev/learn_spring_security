package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.CreateDocumentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateDocumentRequestDTO;
import com.example.spring_security_demo.dto.response.DocumentResponseDTO;
import com.example.spring_security_demo.entity.DocumentEntity;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.DocumentRepository;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.DocumentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public DocumentResponseDTO createDocument(String username, CreateDocumentRequestDTO requestDTO) {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USERNAME_NOT_FOUND,
                        "User not found"
                ));

        DocumentEntity document = new DocumentEntity();
        
        // Never take ownerShipId from the request
        document.setId(user.getId());
        
        document.setTitle(requestDTO.getTitle());
        document.setContent(requestDTO.getContent());
        document.setClassification(requestDTO.getClassification());
        
        // Department comes from the authenticated user's current database attributes 
        document.setDepartment(getDepartment(user));
        
        DocumentEntity saved = documentRepository.save(document);
        return toResponse(saved);
    }

    @Override
    public List<DocumentResponseDTO> getMyDocuments(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USERNAME_NOT_FOUND,
                        "User not found"
                ));
        
        return documentRepository.findAllByOwnerUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DocumentResponseDTO getDocument(Long documentId) {
        
        DocumentEntity document = findDocument(documentId);
        return toResponse(document);
    }

    @Override
    public DocumentResponseDTO updateDocument(Long documentId, UpdateDocumentRequestDTO requestDTO) {
        
        DocumentEntity document = findDocument(documentId);
        
        document.setTitle(requestDTO.getTitle());
        document.setContent(requestDTO.getContent());
        
        DocumentEntity updated = documentRepository.save(document);
        
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {

        DocumentEntity document = findDocument(documentId);
        documentRepository.delete(document);
        
    }
    
    private DocumentEntity findDocument(Long documentId){
        
        return documentRepository.findById(documentId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.NOT_FOUND,
                        "Document Not found !!"
                ));
    }
    
    private String getDepartment(UserEntity user){
        
        if(user.getDepartment() == null || user.getDepartment().isBlank()){
            
            return "GENERAL";
        }
        
        return user.getDepartment().trim().toUpperCase();
    }
    
    private DocumentResponseDTO toResponse(DocumentEntity document){
        
        String ownername = userRepository.findById(document.getOwnerUserId())
                .map(UserEntity::getUsername)
                .orElse("UNKNOWN");
        
        return new DocumentResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getOwnerUserId(),
                ownername,
                document.getDepartment(),
                document.getClassification(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
