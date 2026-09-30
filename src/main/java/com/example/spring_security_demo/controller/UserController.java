package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.dto.request.UpdateUserProfileRequestDTO;
import com.example.spring_security_demo.dto.response.UpdateUserRoleResponseDTO;
import com.example.spring_security_demo.dto.response.UserProfileResponseDTO;
import com.example.spring_security_demo.entity.UserEntity;
import com.example.spring_security_demo.response.ApiResponse;
import com.example.spring_security_demo.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserManagementService userManagementService;

    /*
    * Existing broad RBAC endpoint
     */
    
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getMyProfile(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "My Profile Fetched"
                )
        );
    }

    /*
     * Hybrid RBAC + ABAC
     *
     * ADMIN
     * OR
     * OWN PROFILE
     * OR
     * MODERATOR + same department
     */
    @GetMapping("/profile/{userId}")
    @PreAuthorize("@userAccessPolicy.canReadProfile(authentication, #userId)")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(@PathVariable("userId") Long userId){
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile fetched successfully",
                        userManagementService.getProfile(userId)
                )
        );
    }
    
    // All three roles
//    @GetMapping("/profile")
//    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
//    public ResponseEntity<ApiResponse<?>> getProfile(){
//        return ResponseEntity.ok(
//                ApiResponse.success("Profile fetched successfully")
//        );
//    }
    /*
    * Hybrid ABAC
    * 
    * ADMIN
    * OR 
    * user updating own profile
     */
    @PutMapping("/profile/{userId}")
    @PreAuthorize("@userAccessPolicy.canUpdateProfile(authentication, #userIds")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(
            @PathVariable("userId") Long userId,
            @Valid
            @RequestBody
            UpdateUserProfileRequestDTO requestDTO
    ){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile Updated Successfully !!",
                        userManagementService.updateProfile(userId,requestDTO)
                )
        );
    }
    
    // All three roles
//    @PutMapping("/profile")
//    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
//    public ResponseEntity<ApiResponse<?>> updateProfile(){
//        return ResponseEntity.ok(
//                ApiResponse.success(
//                        "Profile Updated Successfully"
//                )
//        );
//    }
    
    // Pure RBAC demonstration
    // All three roles
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN)")
    public ResponseEntity<ApiResponse<?>> getDashboard(){
        return ResponseEntity.ok(
                ApiResponse.success("User Dashboard Fetched !!")
        );
    }
    
    // MODERATOR AND ADMIN ONLY
    @GetMapping("/moderation")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getModerationPanel(){
        return ResponseEntity.ok(
                ApiResponse.success("Moderation Pannel Fetched !!")
        );
    }
    
    // MODERATOR AND ADMIN ONLY
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteComment(@PathVariable Long commentId){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment deleted successfully",
                        commentId
                )
        );
    }
    
    // ADMIN ONLY
    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAdminReports(){
        return ResponseEntity.ok(
                ApiResponse.success("Admin Reports")
        );
    }
    
    /*
    * Hybrid RBAC + ABAC
    * 
    * ADMIN
    * OR 
    * MODERATOR + SAME DEPARTMENT + TARGET USER
     */
    @DeleteMapping("/delete/user/{userId}")
    @PreAuthorize(
            "@userAccessPolicy.canDeleteUser(authentication, #userId)"
    )
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @PathVariable("userId") Long userId
    ){
     
        userManagementService.deleteUser(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User Deleted !!",
                        userId
                )
        );
    }
    
//    @DeleteMapping("/delete/user/{userId}")
//    @PreAuthorize("@userAccessPolicy.canDeleteUser(authentication, #userId")
//    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long userId){
//        
//        return ResponseEntity.ok(
//                ApiResponse.success("User Deleted", userId)
//        );
//    }
    
}
