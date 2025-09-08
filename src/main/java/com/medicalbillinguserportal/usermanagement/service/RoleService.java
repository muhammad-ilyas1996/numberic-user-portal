package com.medicalbillinguserportal.usermanagement.service;

import com.medicalbillinguserportal.common.validation.DateValidation;
import com.medicalbillinguserportal.commonpersistence.dto.SearchDate;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.*;
import com.medicalbillinguserportal.usermanagement.dto.*;
import com.medicalbillinguserportal.usermanagement.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;

    @Autowired
    private DateValidation dateValidation;
    
    @Transactional
    public RoleDetailDto createRole(RoleCreateDto dto) {
        // Validate role name uniqueness
        if (roleRepository.existsByCodeName(dto.getRoleName())) {
            throw new RuntimeException("Role with name '" + dto.getRoleName() + "' already exists");
        }
        
        // Create role
        Role role = new Role();
        role.setCodeName(dto.getRoleName());
        role.setDisplayName(dto.getDisplayName());
        role.setDescription(dto.getDescription());
        role.setPortalType(portalTypeRepository.findById(dto.getPortalTypeId())
            .orElseThrow(() -> new RuntimeException("Portal type not found")));
        role.setIsSuperadmin(dto.getIsSuperadmin());
        role.setIsReadonly(dto.getIsReadonly());
        role.setIsActive(true);
        
        Role savedRole = roleRepository.save(role);
        
        // Assign permissions
        assignPermissionsToRole(savedRole.getRoleId(), dto.getPermissionIds());
        
        return convertToDetailDto(savedRole);
    }
    
    @Transactional
    public RoleDetailDto updateRole(Long roleId, RoleUpdateDto dto) {
        // Find existing role
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Check if role name is being changed and if it already exists
        if (!role.getCodeName().equals(dto.getRoleName()) && 
            roleRepository.existsByCodeName(dto.getRoleName())) {
            throw new RuntimeException("Role with name '" + dto.getRoleName() + "' already exists");
        }
        
        // Update role fields
        role.setCodeName(dto.getRoleName());
        role.setDisplayName(dto.getDisplayName());
        role.setDescription(dto.getDescription());
        role.setIsSuperadmin(dto.getIsSuperadmin());
        role.setIsReadonly(dto.getIsReadonly());
        
        roleRepository.save(role);
        
        // Update permissions
        updateRolePermissions(roleId, dto.getPermissionIds());
        
        return convertToDetailDto(role);
    }
    
    public Page<RoleListDto> listRoles(RoleListRequestDto request) {
        // Create pageable object
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<Role> spec = com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.equalsValue("isActive", true);
        if (request.getRoleId() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.equalsValue("roleId", request.getRoleId()));
        }
        if (request.getFromDate() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(com.medicalbillinguserportal.commonpersistence.utils.SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }

        // Get roles with pagination
        Page<Role> rolesPage = roleRepository.findAll(spec,
                PageRequest.of(request.getPageNumber(),
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );

        // Convert to DTOs to avoid proxy issues
        List<RoleListDto> roleListDtos = rolesPage.getContent().stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());

        // Return as Page<RoleListDto>
        return new PageImpl<>(roleListDtos,
                PageRequest.of(request.getPageNumber(), request.getPageSize(), Sort.Direction.DESC, "createdOn"),
                rolesPage.getTotalElements());
    }
    
    public RoleDetailDto getRoleDetail(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        return convertToDetailDto(role);
    }

    private void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            System.out.println("=== Assign Permissions Debug ===");
            System.out.println("Role ID: " + roleId);
            System.out.println("Permission IDs: " + permissionIds);
            
            // Get role entity
            Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role with ID " + roleId + " not found"));
            
            for (Long permissionId : permissionIds) {
                System.out.println("Processing permission ID: " + permissionId);
                
                // Get permission entity
                Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permission with ID " + permissionId + " not found"));
                
                RolePermission rolePermission = new RolePermission();
                RolePermissionId id = new RolePermissionId();
                id.setRoleId(roleId);
                id.setPermissionId(permissionId);
                rolePermission.setId(id);
                
                // Set the actual entity relationships
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                
                System.out.println("Saving role permission: " + roleId + " -> " + permissionId);
                rolePermissionRepository.save(rolePermission);
                System.out.println("Role permission saved successfully");
            }
            
            System.out.println("=== End Assign Permissions Debug ===");
            
        } catch (Exception e) {
            System.out.println("Error in assignPermissionsToRole: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // Delete existing permissions
        rolePermissionRepository.deleteByRoleId(roleId);
        
        // Add new permissions
        assignPermissionsToRole(roleId, permissionIds);
    }
    
    private RoleDetailDto convertToDetailDto(Role role) {
        RoleDetailDto dto = new RoleDetailDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getCodeName());
        dto.setDisplayName(role.getDisplayName());
        dto.setDescription(role.getDescription());
        dto.setPortalTypeId(role.getPortalType().getPortalTypeId());
        dto.setPortalTypeName(role.getPortalType().getPortalName());
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsReadonly(role.getIsReadonly());
        dto.setIsActive(role.getIsActive());
        
        // Get permissions for this role
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
        List<PermissionDto> permissions = rolePermissions.stream()
                .map(rp -> convertPermissionToDto(rp.getPermission()))
                .collect(Collectors.toList());
        dto.setPermissions(permissions);
        
        return dto;
    }
    
    private RoleListDto convertToListDto(Role role) {
        RoleListDto dto = new RoleListDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getCodeName());
        dto.setDisplayName(role.getDisplayName());
        dto.setDescription(role.getDescription());
        dto.setPortalTypeName(role.getPortalType().getPortalName());
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsReadonly(role.getIsReadonly());
        dto.setIsActive(role.getIsActive());
        
        // Get permission count
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
        dto.setPermissionCount(rolePermissions.size());
        
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
        return dto;
    }
}
