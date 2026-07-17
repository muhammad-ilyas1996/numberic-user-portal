package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.registration.validation.PasswordValidator;
import com.numbericsuserportal.usermanagement.domain.*;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    
    @Autowired
    private PasswordValidator passwordValidator;
    
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
        
        // Get available roles for NUMBRICS portal
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
            
        List<Role> availableRoles = roleRepository.findByPortalTypeAndIsActiveTrue(numbricsPortal);
        dto.setAvailableRoles(availableRoles.stream()
                .map(this::convertRoleToDto)
                .collect(Collectors.toList()));
        
        // Get available permissions for NUMBRICS portal
        List<Permission> availablePermissions = permissionRepository.findByPortalTypeAndIsActiveTrue(numbricsPortal);
        dto.setAvailablePermissions(availablePermissions.stream()
                .map(this::convertPermissionToDto)
                .collect(Collectors.toList()));

        // ── Subscription / Billing fields ──────────────────────────────────
        dto.setSubscriptionStatus(user.getSubscriptionStatus());
        dto.setSubscriptionPlan(
                user.getSubscriptionPlan() != null ? user.getSubscriptionPlan().name() : null);
        dto.setPaymentCompleted(user.getPaymentCompleted());
        dto.setTrialStartDate(user.getTrialStartDate());
        dto.setPaymentDueDate(user.getPaymentDueDate());
        
        return dto;
    }
    
    // Get all users
    public List<UserWithPermissionsDto> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        return users.stream()
                .map(user -> getUserWithPermissions(user.getUserId()))
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
        
        return getUserWithPermissions(assignmentDto.getUserId());
    }
    
    /**
     * Update user profile (email, firstName, lastName, phone)
     * Only authenticated user can update their own profile
     */
    @Transactional
    public UserWithPermissionsDto updateProfile(Long userId, UpdateProfileRequest request) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate user is active and not deleted
        if (Boolean.TRUE.equals(user.getIsDeleted()) || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("User is inactive or deleted");
        }
        
        // Check email uniqueness (exclude current user)
        Optional<User> existingUser = userRepository.findByEmailAndUserIdIsNot(request.getEmail(), userId);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Update fields
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone()); // Can be null (optional)
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user
        userRepository.save(user);
        
        // Return updated user with permissions
        return getUserWithPermissions(userId);
    }
    
    /**
     * Change user password
     * Requires current password verification
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate user is active and not deleted
        if (Boolean.TRUE.equals(user.getIsDeleted()) || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("User is inactive or deleted");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Check if new password is same as current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }
        
        // Validate new password strength
        if (!passwordValidator.isValid(request.getNewPassword(), null)) {
            throw new RuntimeException("Password must be at least 10 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user
        userRepository.save(user);
    }
    
    // Helper methods
    private List<Permission> getUserPermissions(Long userId) {
        // Get user roles with role data loaded
        List<UserRole> userRoles = userRoleRepository.findByUserUserIdAndIsActiveTrue(userId);
        
        // Get permissions from roles
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
                    // Log error but continue processing
                    System.err.println("Error fetching permissions for role " + role.getRoleId() + ": " + e.getMessage());
                }
            });
        
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
