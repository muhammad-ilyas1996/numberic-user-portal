package com.medicalbillinguserportal.usermanagement.controller;

import com.medicalbillinguserportal.usermanagement.dto.*;
import com.medicalbillinguserportal.usermanagement.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-management")
@CrossOrigin(origins = "*")
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    // Create new user with role and permissions
    @PostMapping("/users")
    public ResponseEntity<UserWithPermissionsDto> createUser(
            @RequestBody UserCreateDto userCreateDto,
            @RequestParam String portalType,
            @RequestParam Long createdBy) {
        try {
            UserWithPermissionsDto createdUser = userManagementService.createUser(userCreateDto, portalType, createdBy);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get user with all permissions and available options
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserWithPermissionsDto> getUserWithPermissions(
            @PathVariable Long userId,
            @RequestParam String portalType) {
        try {
            UserWithPermissionsDto user = userManagementService.getUserWithPermissions(userId, portalType);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get all users for a portal
    @GetMapping("/users")
    public ResponseEntity<List<UserWithPermissionsDto>> getAllUsersForPortal(
            @RequestParam String portalType) {
        try {
            List<UserWithPermissionsDto> users = userManagementService.getAllUsersForPortal(portalType);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update user permissions
    @PutMapping("/users/{userId}/permissions")
    public ResponseEntity<UserWithPermissionsDto> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody UserPermissionAssignmentDto assignmentDto,
            @RequestParam Long updatedBy) {
        try {
            assignmentDto.setUserId(userId);
            UserWithPermissionsDto updatedUser = userManagementService.updateUserPermissions(assignmentDto, updatedBy);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Assign role to user
    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestParam Long assignedBy) {
        try {
            userManagementService.assignRoleToUser(userId, roleId, assignedBy);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


