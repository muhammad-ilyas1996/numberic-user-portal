package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithPermissionsDto {
    
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String npi;
    private String licenseNumber;
    private Boolean isPracticeAdmin;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    // Current role
    private RoleDto currentRole;
    
    // All permissions (from role + additional)
    private List<PermissionDto> allPermissions;
    
    // Available roles for this portal
    private List<RoleDto> availableRoles;
    
    // Available permissions for this portal
    private List<PermissionDto> availablePermissions;
}


