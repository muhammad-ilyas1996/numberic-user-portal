package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.service.UserManagementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-management")
@CrossOrigin(origins = "*")
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private UserManagementService userManagementService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get current user's permissions and info (for frontend)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserInfo(
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "User not authenticated"
            ));
        }
        try {
            UserWithPermissionsDto user = userManagementService.getUserWithPermissions(currentUser.getUserId());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("Error getting user info for userId: {}", currentUser.getUserId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "User not found"
            ));
        } catch (Exception e) {
            logger.error("Error getting user info", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to retrieve user info"
            ));
        }
    }
    
    // Get user with all permissions and available options
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserWithPermissionsDto> getUserWithPermissions(
            @PathVariable Long userId) {
        try {
            UserWithPermissionsDto user = userManagementService.getUserWithPermissions(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<UserWithPermissionsDto>> getAllUsers() {
        try {
            List<UserWithPermissionsDto> users = userManagementService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET /api/user-management/profile
     * Get current user's profile (email, firstName, lastName, phone, username)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "User not authenticated"
            ));
        }
        
        try {
            User user = userRepository.findById(currentUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> profile = Map.of(
                "email", user.getEmail() != null ? user.getEmail() : "",
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName", user.getLastName() != null ? user.getLastName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "username", user.getUsername() != null ? user.getUsername() : ""
            );
            
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            logger.error("Error getting profile for userId: {}", currentUser.getUserId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to retrieve profile"
            ));
        } catch (Exception e) {
            logger.error("Error getting profile", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to retrieve profile. Please try again."
            ));
        }
    }
    
    /**
     * PUT /api/user-management/profile
     * Update current user's profile (email, firstName, lastName, phone)
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "User not authenticated"
            ));
        }
        
        try {
            userManagementService.updateProfile(
                currentUser.getUserId(), 
                request
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile has been updated successfully."
            ));
        } catch (RuntimeException e) {
            logger.error("Error updating profile for userId: {}", currentUser.getUserId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to update profile"
            ));
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update profile. Please try again."
            ));
        }
    }
    
    /**
     * PUT /api/user-management/change-password
     * Change current user's password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "User not authenticated"
            ));
        }
        
        try {
            userManagementService.changePassword(currentUser.getUserId(), request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password has been changed successfully."
            ));
        } catch (RuntimeException e) {
            logger.error("Error changing password for userId: {}", currentUser.getUserId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to change password"
            ));
        } catch (Exception e) {
            logger.error("Error changing password", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to change password. Please try again."
            ));
        }
    }
}


