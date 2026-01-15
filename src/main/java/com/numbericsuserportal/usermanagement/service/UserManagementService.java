package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.usermanagement.domain.*;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
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
    public UserWithPermissionsDto createUser(UserCreateDto userCreateDto, Long createdBy) {
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
        
        return getUserWithPermissions(savedUser.getUserId());
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
    public UserWithPermissionsDto getUserWithPermissions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        UserWithPermissionsDto dto = buildUserDto(user);
        dto.setCurrentRole(getUserRole(userId));
        dto.setAllPermissions(getUserPermissions(userId));
        loadPortalData(dto);
        
        return dto;
    }
    
    private UserWithPermissionsDto buildUserDto(User user) {
        UserWithPermissionsDto dto = new UserWithPermissionsDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
    
    private RoleDto getUserRole(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
            .findFirst()
            .map(UserRole::getRole)
            .filter(Objects::nonNull)
            .flatMap(role -> roleRepository.findById(role.getRoleId()))
            .map(this::convertRoleToDto)
            .orElse(null);
    }
    
    private void loadPortalData(UserWithPermissionsDto dto) {
        portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .ifPresentOrElse(
                portal -> {
                    dto.setAvailableRoles(getActiveRoles(portal));
                    dto.setAvailablePermissions(getActivePermissions(portal));
                },
                () -> {
                    logger.warn("NUMBRICS Portal not found");
                    dto.setAvailableRoles(Collections.emptyList());
                    dto.setAvailablePermissions(Collections.emptyList());
                }
            );
    }
    
    private List<RoleDto> getActiveRoles(PortalType portal) {
        return roleRepository.findByPortalTypeAndIsActiveTrue(portal).stream()
                .map(this::convertRoleToDto)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
        
    private List<PermissionDto> getActivePermissions(PortalType portal) {
        return permissionRepository.findByPortalTypeAndIsActiveTrue(portal).stream()
                .map(this::convertPermissionToDto)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    // Get all users
    public List<UserWithPermissionsDto> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        
        return users.stream()
            .map(this::getUserWithPermissionsSafely)
            .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private UserWithPermissionsDto getUserWithPermissionsSafely(User user) {
        try {
            return getUserWithPermissions(user.getUserId());
        } catch (Exception e) {
            logger.warn("Failed to load full permissions for user {}: {}", user.getUserId(), e.getMessage());
            return buildBasicUserDto(user);
        }
    }
    
    private UserWithPermissionsDto buildBasicUserDto(User user) {
        UserWithPermissionsDto dto = buildUserDto(user);
        dto.setAllPermissions(Collections.emptyList());
        dto.setAvailableRoles(Collections.emptyList());
        dto.setAvailablePermissions(Collections.emptyList());
        return dto;
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
        
        return getUserWithPermissions(assignmentDto.getUserId());
    }
    
    // Helper methods
    private List<PermissionDto> getUserPermissions(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserUserIdAndIsActiveTrue(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<Permission> allPermissions = new HashSet<>();
        userRoles.stream()
            .filter(Objects::nonNull)
            .map(UserRole::getRole)
            .filter(Objects::nonNull)
            .forEach(role -> {
                try {
                    List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
                    if (rolePermissions != null && !rolePermissions.isEmpty()) {
                        rolePermissions.stream()
                            .filter(Objects::nonNull)
                            .map(RolePermission::getPermission)
                            .filter(Objects::nonNull)
                            .forEach(allPermissions::add);
                    }
                } catch (Exception e) {
                    logger.warn("Error fetching permissions for role {}: {}", role.getRoleId(), e.getMessage());
                }
            });
        
        return allPermissions.stream()
                .map(this::convertPermissionToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private RoleDto convertRoleToDto(Role role) {
        if (role == null) {
            return null;
        }
        RoleDto dto = new RoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setCodeName(role.getCodeName());
        dto.setDisplayName(role.getDisplayName());
        dto.setDescription(role.getDescription());
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsActive(role.getIsActive());
        dto.setIsReadonly(role.getIsReadonly());
        
        Optional.ofNullable(role.getPortalType()).ifPresent(portal -> {
            dto.setPortalTypeId(portal.getPortalTypeId());
            dto.setPortalTypeName(portal.getPortalName());
        });
        
        return dto;
    }
    
    private PermissionDto convertPermissionToDto(Permission permission) {
        if (permission == null) {
            return null;
        }
        PermissionDto dto = new PermissionDto();
        dto.setPermissionId(permission.getPermissionId());
        dto.setCodeName(permission.getCodeName());
        dto.setDisplayName(permission.getDisplayName());
        dto.setCategory(permission.getCategory());
        dto.setDescription(permission.getDescription());
        dto.setIsSuperadmin(permission.getIsSuperadmin());
        dto.setIsActive(permission.getIsActive());
        
        Optional.ofNullable(permission.getPortalType()).ifPresent(portal -> {
            dto.setPortalTypeId(portal.getPortalTypeId());
            dto.setPortalTypeName(portal.getPortalName());
        });
        
        return dto;
    }
}
