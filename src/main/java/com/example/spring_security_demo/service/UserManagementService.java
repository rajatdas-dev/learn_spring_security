package com.example.spring_security_demo.service;

import com.example.spring_security_demo.dto.request.UpdateUserDepartmentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateUserProfileRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.dto.response.UserProfileResponseDTO;

public interface UserManagementService {
    
    public UpdateUserRoleResponseDTO updateRole(UpdateUserRoleRequestDTO updateUserRoleRequestDTO);
    
    UserProfileResponseDTO getProfile(Long userId);
    
    UserProfileResponseDTO updateProfile(Long userId, UpdateUserProfileRequestDTO requestDTO);
    
    UserProfileResponseDTO updateDepartment(Long userId, UpdateUserDepartmentRequestDTO requestDTO);
    
    void deleteUser(Long userId);
}
