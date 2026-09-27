package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.exception.ErrorCode;
import com.example.spring_security_demo.exception.ResourceNotFoundException;
import com.example.spring_security_demo.repository.UserRepository;
import com.example.spring_security_demo.service.UserManagementService;
import com.example.spring_security_demo.service.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    @Override
    public UpdateUserRoleResponseDTO updateRole(UpdateUserRoleRequestDTO updateUserRoleRequestDTO) {

        UserEntity userEntity = userRepository.findById(updateUserRoleRequestDTO.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User Not found !!"
                ));
        
        userEntity.setRole(updateUserRoleRequestDTO.getUserRole().name().toUpperCase());
        
        UserEntity updatedUser =  userRepository.save(userEntity);
        
        UpdateUserRoleResponseDTO updateUserRoleResponseDTO = new UpdateUserRoleResponseDTO();
        
        updateUserRoleResponseDTO.setUserId(updatedUser.getId());
        updateUserRoleResponseDTO.setUserRole(updatedUser.getRole());
        updateUserRoleResponseDTO.setMessage("User "+ updatedUser.getUsername()+ " updated");
        
        return updateUserRoleResponseDTO;
        
    }
}
