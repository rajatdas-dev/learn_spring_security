package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.dto.request.CreateDocumentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateDocumentRequestDTO;
import com.example.spring_security_demo.dto.response.DocumentResponseDTO;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    /*
    * Any authenticated user can create a document
    * 
    * Owner and department are derived server side
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> createDocument(
            Authentication authentication,
            @Valid
            @RequestBody
            CreateDocumentRequestDTO requestDTO
    ){
        DocumentResponseDTO responseDTO = documentService.createDocument(authentication.getName(),requestDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document created successfully",
                        responseDTO
                )
        );
    }
    
    // Every user can see their own documents
    
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentResponseDTO>>> getMyDocuments(Authentication authentication){
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Documents fetched successfully !!",
                        documentService.getMyDocuments(
                                authentication.getName()
                        )
                )
        );
    }
    
    /*
    * ABAC: 
    * 
    * Subject + Resource + Action
     */
    @GetMapping("/{documentId}")
    @PreAuthorize(
            "@documentAccessPolicy.canRead(authentication, #documentId)"
    )
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> getDocument(
            @PathVariable("documentId") Long documentId
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document fetched successfully",
                        documentService.getDocument(
                                documentId
                        )
                )
        );
    }
    
    /*
    * ABAC: 
    * 
    * Subject + Resource + Update
     */
    @PutMapping("{documentId}")
    @PreAuthorize(
            "@documentAccessPolicy.canUpdate(authentication, #documentId)"
    )
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> updateDocument(
            @PathVariable("documentId") Long documentId,
            @Valid
            @RequestBody
            UpdateDocumentRequestDTO requestDTO
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document updated successfully",
                        documentService.updateDocument(
                                documentId,
                                requestDTO
                        )
                )
        );
    }
    
    /*
    * ABAC:
    * 
    * Subject + Resource + Delete
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize(
            "@documentAccessPolicy.canDelete(authentication, #documentId)"
    )
    public ResponseEntity<ApiResponse<?>> deleteDocument(
            @PathVariable("documentId") Long documentId
    ){
        
        documentService.deleteDocument(documentId);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document deleted successfully",
                        documentId
                )
        );
    }
}
