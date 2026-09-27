package com.example.spring_security_demo.controller;

import com.example.spring_security_demo.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    
    // All three roles
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getProfile(){
        return ResponseEntity.ok(
                ApiResponse.success("Profile fetched successfully")
        );
    }
    
    // All three roles
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','MODERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateProfile(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile Updated Successfully"
                )
        );
    }
    
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
    
    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long userId){
        
        return ResponseEntity.ok(
                ApiResponse.success("User Deleted", userId)
        );
    }
    
}
