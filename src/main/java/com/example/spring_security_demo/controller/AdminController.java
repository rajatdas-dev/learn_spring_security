package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.dto.request.UpdateUserDepartmentRequestDTO;
import com.example.spring_security_demo.dto.request.UpdateUserRoleRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.dto.response.UserProfileResponseDTO;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    /*
    * RBAC:
    * 
    * Only Admin can change roles
     */
    @PatchMapping("/update/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UpdateUserRoleResponseDTO>> updateUserRole(
            @Valid  @RequestBody UpdateUserRoleRequestDTO updateUserRoleRequestDTO){
        
       UpdateUserRoleResponseDTO updateUserRoleResponseDTO = userManagementService.updateRole(updateUserRoleRequestDTO);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User Updated",
                        updateUserRoleResponseDTO
                )
        );
    }
    
    /*
    * RBAC: 
    * 
    * Only Admin can change a user's department attribute
     */
    @PatchMapping("/{userId}/department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateDepartment(
            @PathVariable("userId") Long userId,
            @Valid
            @RequestBody
            UpdateUserDepartmentRequestDTO requestDTO
    ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User department updated",
                        userManagementService.updateDepartment(
                                userId,
                                requestDTO
                        )
                )
        );
    }
}
