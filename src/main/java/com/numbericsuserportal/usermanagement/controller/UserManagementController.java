package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-management")
@CrossOrigin(origins = "*")
public class UserManagementController {
    
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
    public ResponseEntity<List<UserWithPermissionsDto>> getAllUsers() {
        try {
            List<UserWithPermissionsDto> users = userManagementService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


