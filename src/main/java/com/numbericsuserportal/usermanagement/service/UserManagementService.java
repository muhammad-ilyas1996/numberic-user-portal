package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.usermanagement.domain.*;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Create new user with role and permissions
    @Transactional
    public UserWithPermissionsDto createUser(UserCreateDto userCreateDto, String portalType, Long createdBy) {
        // Check if user already exists
        if (userRepository.existsByUsername(userCreateDto.getUsername()) || 
            userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new RuntimeException("User with this username or email already exists");
        }
        
        // Create user
        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setFirstName(userCreateDto.getFirstName());
        user.setLastName(userCreateDto.getLastName());
        user.setPhone(userCreateDto.getPhone());
        user.setIsActive(true);
        user.setIsDeleted(false);
        // BaseEntity automatically manages these fields
        // No need to set them manually
        
        User savedUser = userRepository.save(user);
        
        // Assign role
        if (userCreateDto.getRoleId() != null) {
            assignRoleToUser(savedUser.getUserId(), userCreateDto.getRoleId(), createdBy);
        }
        
        // Assign additional permissions if any
        if (userCreateDto.getAdditionalPermissionIds() != null && !userCreateDto.getAdditionalPermissionIds().isEmpty()) {
            assignAdditionalPermissionsToUser(savedUser.getUserId(), userCreateDto.getAdditionalPermissionIds(), createdBy);
        }
        
        return getUserWithPermissions(savedUser.getUserId(), portalType);
    }
    
    // Assign role to user
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, Long assignedBy) {
        // Remove existing role assignments
        userRoleRepository.deleteByUserId(userId);
        
        // Create new role assignment
        UserRole userRole = new UserRole();
        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(userId);
        userRoleId.setRoleId(roleId);
        userRole.setId(userRoleId);
//        userRole.setIsActive(true);
        // BaseEntity automatically manages these fields
        // No need to set them manually
        
        userRoleRepository.save(userRole);
    }
    
    // Assign additional permissions to user (beyond role permissions)
    @Transactional
    public void assignAdditionalPermissionsToUser(Long userId, List<Long> permissionIds, Long assignedBy) {
        // This would require a user_permissions table for direct user-permission mapping
        // For now, we'll create a custom role with these permissions
        // Implementation depends on your specific requirements
    }
    
    // Get user with all permissions and available options
    public UserWithPermissionsDto getUserWithPermissions(Long userId, String portalType) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        UserWithPermissionsDto dto = new UserWithPermissionsDto();
        
        // Basic user info
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin());
        // BaseEntity fields are automatically managed
        // No need to get them from entity
        
        // Get current role
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (!userRoles.isEmpty()) {
            UserRole userRole = userRoles.get(0);
            Optional<Role> roleOpt = roleRepository.findById(userRole.getRole().getRoleId());
            if (roleOpt.isPresent()) {
                dto.setCurrentRole(convertRoleToDto(roleOpt.get()));
            }
        }
        
        // Get all permissions for this user
        List<Permission> userPermissions = getUserPermissions(userId);
        dto.setAllPermissions(userPermissions.stream()
                .map(this::convertPermissionToDto)
                .collect(Collectors.toList()));
        
        // Get available roles for this portal
        Long portalTypeId = Long.valueOf(portalType);
        PortalType portalTypeEntity = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Role> availableRoles = roleRepository.findByPortalTypeAndIsActiveTrue(portalTypeEntity);
        dto.setAvailableRoles(availableRoles.stream()
                .map(this::convertRoleToDto)
                .collect(Collectors.toList()));
        
        // Get available permissions for this portal
        List<Permission> availablePermissions = permissionRepository.findByPortalTypeAndIsActiveTrue(portalTypeEntity);
        dto.setAvailablePermissions(availablePermissions.stream()
                .map(this::convertPermissionToDto)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    // Get all users for a portal
    public List<UserWithPermissionsDto> getAllUsersForPortal(String portalType) {
        List<User> users = userRepository.findByIsDeletedFalse();
        return users.stream()
                .map(user -> getUserWithPermissions(user.getUserId(), portalType))
                .collect(Collectors.toList());
    }
    
    // Update user permissions
    @Transactional
    public UserWithPermissionsDto updateUserPermissions(UserPermissionAssignmentDto assignmentDto, Long updatedBy) {
        // Update role
        if (assignmentDto.getRoleId() != null) {
            assignRoleToUser(assignmentDto.getUserId(), assignmentDto.getRoleId(), updatedBy);
        }
        
        // Update additional permissions if needed
        if (assignmentDto.getPermissionIds() != null && !assignmentDto.getPermissionIds().isEmpty()) {
            assignAdditionalPermissionsToUser(assignmentDto.getUserId(), assignmentDto.getPermissionIds(), updatedBy);
        }
        
        return getUserWithPermissions(assignmentDto.getUserId(), assignmentDto.getPortalType());
    }
    
    // Helper methods
    private List<Permission> getUserPermissions(Long userId) {
        // Get user roles with role data loaded
        List<UserRole> userRoles = userRoleRepository.findByUserUserIdAndIsActiveTrue(userId);
        
        // Get permissions from roles with detailed logging
        Set<Permission> allPermissions = new HashSet<>();
        for (UserRole userRole : userRoles) {
            System.out.println("=== UserRole Debug ===");
            System.out.println("Role ID: " + userRole.getRole().getRoleId());
            System.out.println("Role Name: " + userRole.getRole().getRoleId());
            
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(userRole.getRole().getRoleId());
            System.out.println("Role Permissions Count: " + rolePermissions.size());
            
            for (RolePermission rolePermission : rolePermissions) {
                System.out.println("Permission ID: " + rolePermission.getPermission().getPermissionId());
                System.out.println("Permission Name: " + rolePermission.getPermission().getPermissionId());
                allPermissions.add(rolePermission.getPermission());
            }
            System.out.println("=== End UserRole Debug ===");
        }
        
        return new ArrayList<>(allPermissions);
    }
    
    private RoleDto convertRoleToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setCodeName(role.getCodeName());
        dto.setDisplayName(role.getDisplayName());
        dto.setDescription(role.getDescription());
        dto.setPortalTypeId(role.getPortalType().getPortalTypeId());
        dto.setPortalTypeName(role.getPortalType().getPortalName());
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsActive(role.getIsActive());
        dto.setIsReadonly(role.getIsReadonly());
        // BaseEntity fields are automatically managed
        // No need to get them from entity
        return dto;
    }
    
    private PermissionDto convertPermissionToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPermissionId(permission.getPermissionId());
        dto.setCodeName(permission.getCodeName());
        dto.setDisplayName(permission.getDisplayName());
        dto.setCategory(permission.getCategory());
        dto.setDescription(permission.getDescription());
        dto.setPortalTypeId(permission.getPortalType().getPortalTypeId());
        dto.setPortalTypeName(permission.getPortalType().getPortalName());
        dto.setIsSuperadmin(permission.getIsSuperadmin());
        dto.setIsActive(permission.getIsActive());
        // BaseEntity fields are automatically managed
        // No need to get them from entity
        return dto;
    }
}
