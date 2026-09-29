package com.example.spring_security_demo.authorization;

import com.example.spring_security_demo.entity.DocumentEntity;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.DocumentRepository;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.DocumentClassification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;

@Component("documentAccessPolicy")
public class DocumentAccessPolicy {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public boolean canRead(
            Authentication authentication,
            Long documentId
    ){

        Optional<DocumentEntity> documentOptional = documentRepository.findById(documentId);
        
        if(documentOptional.isEmpty()){
            return false;
        }
        
        UserEntity user = getCurrentUser(authentication);
        
        if(isAdmin(user)){
            return true;
        }
        
        DocumentEntity document = documentOptional.get();
        
        /*
        * Subject Attribute 
        * User ID
        * 
        * Resource attribute 
        * Owner ID
         */
        if(isOwner(user,document)){
            return true;
        }
        
        /*
        * Moderator 
        * 
        * Subject department == Resource Department 
        * 
        * and 
        * 
        * classification != Confidential
         */
        
        if(isModerator(user)){
            return sameDepartment(user,document)
                    && document.getClassification() != DocumentClassification.CONFIDENTIAL;
        }
        
        /*
        * User : 
        * Can read Public documents in their department
         */
        
        return sameDepartment(user,document)
                && document.getClassification() == DocumentClassification.PUBLIC;
    }
    
    
    
    private boolean canUpdate(
            Authentication authentication,
            Long documentId
    ){
        
        Optional<DocumentEntity> documentOptional = documentRepository.findById(documentId);
        
        if(documentOptional.isEmpty()){
            return false;
        }
        
        UserEntity user = getCurrentUser(authentication);
        
        if(isAdmin(user)){
            return true;
        }
        
        DocumentEntity document = documentOptional.get();
        
        // Owner can update their own resource 
        if(isOwner(user,document)){
            return true;
        }
        
        // Moderator can modify non-confidential documents in their own department 
        return isModerator(user)
                && sameDepartment(user,document)
                && document.getClassification() != DocumentClassification.CONFIDENTIAL;
        
    }
    
    private boolean canDelete(
            Authentication authentication,
            Long documentId
    ){

        Optional<DocumentEntity> documentOptional = documentRepository.findById(documentId);
        
        if(documentOptional.isEmpty()){
            return  false;
        }
        
        UserEntity user = getCurrentUser(authentication);
        
        if(isAdmin(user)){
            return true;
        }
        
        DocumentEntity document = documentOptional.get();
        
        // Owner can delete user 
        
        if(isOwner(user, document)){
            return true;
        }
        
        // Moderator can delete non-confidential documents in same department 
        return isModerator(user)
                && sameDepartment(user,document)
                && document.getClassification() != DocumentClassification.CONFIDENTIAL;
    }
    
    private UserEntity getCurrentUser(
            Authentication authentication
    ){
        return userRepository.findByUsername(
                authentication.getName()
        ).orElseThrow(()-> new ResourceNotFoundException(
                ErrorCode.USERNAME_NOT_FOUND,
                "User not found !!"
        ));
    }
    
    private boolean isOwner(
            UserEntity user,
            DocumentEntity document
    ){
        
        return user.getId().equals(
                document.getOwnerUserId()
        );
    }
    
    private boolean sameDepartment(
            UserEntity user,
            DocumentEntity document
    ){
        
        String userDepartment = normalizeDepartment(
                user.getDepartment()
        );
        
        String documentDepartment = normalizeDepartment(
                document.getDepartment()
        );
        
        return userDepartment.equals(documentDepartment);
        
        
    }
    
    private boolean isAdmin(UserEntity user){
        
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
    
    private boolean isModerator(UserEntity user){
        
        return "MODERATOR".equalsIgnoreCase(user.getRole());
    }
    
    private boolean sameDepartment(
            UserEntity first, 
            UserEntity second
    ){
        
        return normalizeDepartment(
                first.getDepartment()
        ).equals(
                normalizeDepartment(second.getDepartment())
        );
    }
    
    
    private String normalizeDepartment(
            String department 
    ){
        
        if(department == null || department.isBlank()){
            return "GENERAL";
        }
        
        return department.trim().toUpperCase();
    }
}
