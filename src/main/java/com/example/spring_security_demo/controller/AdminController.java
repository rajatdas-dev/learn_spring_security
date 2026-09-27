package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    @PatchMapping("/update/role")
    public ResponseEntity<ApiResponse<UpdateUserRoleResponseDTO>> updateUserRole(@RequestBody UpdateUserRoleRequestDTO updateUserRoleRequestDTO){
        
       UpdateUserRoleResponseDTO updateUserRoleResponseDTO = userManagementService.updateRole(updateUserRoleRequestDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User Updated",
                        updateUserRoleResponseDTO
                )
        );
    }
}
