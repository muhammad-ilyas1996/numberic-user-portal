package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    // Get current user's permissions and info (for frontend)
    @GetMapping("/me")
    public ResponseEntity<UserWithPermissionsDto> getCurrentUserInfo(
            @AuthenticationPrincipal User currentUser) {
        try {
            UserWithPermissionsDto user = userManagementService.getUserWithPermissions(currentUser.getUserId());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserWithPermissionsDto> users = userManagementService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "Failed to retrieve users"
            ));
        }
    }
}


