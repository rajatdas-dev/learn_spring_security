package com.example.spring_security_demo.service.impl;

import com.example.spring_security_demo.dto.request.UpdateUserDepartmentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateUserProfileRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.dto.response.UserProfileResponseDTO;
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

    @Override
    public UserProfileResponseDTO getProfile(Long userId) {
        return toProfileResponse(findUser(userId));
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(Long userId, UpdateUserProfileRequestDTO requestDTO) {
        
        UserEntity user = findUser(userId);
        
        user.setDisplayName(requestDTO.getDisplayName());
        return toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateDepartment(Long userId, UpdateUserDepartmentRequestDTO requestDTO) {
        
        UserEntity user = findUser(userId);
        
        user.setDepartment(
                requestDTO.getDepartment().trim().toUpperCase()
        );
        
        return toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        
        UserEntity user = findUser(userId);
        
        userRepository.delete(user);

    }
    
    private UserEntity findUser(Long userId){
        
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User Not found !!"
                ));
        
    }
    
    private UserProfileResponseDTO toProfileResponse(
            UserEntity user
    ){
        
        String department = user.getDepartment();
        
        if(department == null || department.isBlank()){
            department = "GENERAL";
        }
        
        return new UserProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                department
        );
    }
}
