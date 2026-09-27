package com.example.spring_security_demo.service;

import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;

public interface UserManagementService {
    
    public UpdateUserRoleResponseDTO updateRole(UpdateUserRoleRequestDTO updateUserRoleRequestDTO);
}
