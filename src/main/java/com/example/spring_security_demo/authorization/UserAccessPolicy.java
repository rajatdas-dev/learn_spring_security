package com.example.spring_security_demo.authorization;

import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("userAccessPolicy")
public class UserAccessPolicy {
    
    @Autowired
    private UserRepository userRepository;
    
    public boolean canReadProfile(
            Authentication authentication,
            Long targetUserId
    ){
        
        Optional<UserEntity> targetOptional = userRepository.findById(targetUserId);
        
        if(targetOptional.isEmpty()){
            return false;
        }
        
        UserEntity currentUser = getCurrentUser(authentication);
        
        UserEntity targetUser = targetOptional.get();
        
        // RBAC condition 
        if(isAdmin(currentUser)){
            return true;
        }
        
        // ABAC Condition : Subject Id == Target Owner Id
        if(currentUser.getId().equals(targetUser.getId())){
            return true;
        }
        
        // Hybrid ABAC + RBAC
        return isModerator(currentUser) 
                && sameDepartment(currentUser, targetUser);
    }
    
    public boolean canUpdateProfile(
            Authentication authentication,
            Long targetId
    ){

        Optional<UserEntity> targetOptional = userRepository.findById(targetId);
        
        if(targetOptional.isEmpty()){
            return false;
        }
        
        UserEntity currentUser = getCurrentUser(authentication);
        
        // Admin can update another user's profile 
        if(isAdmin(currentUser)){
            return true;
        }
        
        // ABAC: Subject id == Resource ID
        return currentUser.getId().equals(targetId);
    }
    
    public boolean canDeleteUser(
            Authentication authentication,
            Long targetId
    ){

        Optional<UserEntity> targetOptional = userRepository.findById(targetId);
        
        if(targetOptional.isEmpty()){
            return false;
        }
        
        UserEntity currentUser = getCurrentUser(authentication);
        
        UserEntity targetUser = targetOptional.get();
        
        // Don't allow self deletion through this administrative endpoint
        if(currentUser.getId().equals(targetUser.getId())){
            return false;
        }
        
        // Admin can delete another user
        if(isAdmin(currentUser)){
            return true;
        }
        
        // Moderator 
        return isModerator(currentUser)
                && sameDepartment(currentUser, targetUser)
                && "USER".equalsIgnoreCase(targetUser.getRole());
    }
    
    private UserEntity getCurrentUser(
            Authentication authentication
    ){
        
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USERNAME_NOT_FOUND,
                        "User not found !!"
                ));
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
        return normalizeDepartment(first.getDepartment())
                .equals(second.getDepartment());
    }
    
    private String normalizeDepartment(String department){
        if(department == null || department.isBlank()){
            return "GENERAL";
        }
        
        return department.trim().toUpperCase();
    }
}
