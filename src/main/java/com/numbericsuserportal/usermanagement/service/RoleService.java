package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.common.validation.DateValidation;
import com.numbericsuserportal.commonpersistence.dto.SearchDate;
import com.numbericsuserportal.usermanagement.domain.*;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageImpl;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
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
        // Set NUMBRICS portal type
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
        role.setPortalType(numbricsPortal);
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
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<Role> spec = buildRoleSpecification(request, searchDate);
        
        int pageNumber = Optional.ofNullable(request.getPageNumber()).orElse(0);
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(10);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createdOn");
        
        Page<Role> rolesPage = roleRepository.findAll(spec, pageRequest);
        List<RoleListDto> roleListDtos = rolesPage.getContent().stream()
            .map(this::convertToListDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(roleListDtos, pageRequest, rolesPage.getTotalElements());
    }
    
    private Specification<Role> buildRoleSpecification(RoleListRequestDto request, SearchDate searchDate) {
        Specification<Role> spec = com.numbericsuserportal.commonpersistence.utils.SpecificationUtility.equalsValue("isActive", true);
        
        if (request.getRoleId() != null) {
            spec = spec.and(com.numbericsuserportal.commonpersistence.utils.SpecificationUtility.equalsValue("roleId", request.getRoleId()));
        }
        if (searchDate.getFromDate() != null) {
            spec = spec.and(com.numbericsuserportal.commonpersistence.utils.SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (searchDate.getToDate() != null) {
            spec = spec.and(com.numbericsuserportal.commonpersistence.utils.SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        
        return spec;
    }
    
    public RoleDetailDto getRoleDetail(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        return convertToDetailDto(role);
    }

    private void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role with ID " + roleId + " not found"));
        
        permissionIds.forEach(permissionId -> {
            Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission with ID " + permissionId + " not found"));
            
            RolePermission rolePermission = new RolePermission();
            RolePermissionId id = new RolePermissionId();
            id.setRoleId(roleId);
            id.setPermissionId(permissionId);
            rolePermission.setId(id);
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            
            rolePermissionRepository.save(rolePermission);
        });
        
        logger.debug("Assigned {} permissions to role {}", permissionIds.size(), roleId);
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
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsReadonly(role.getIsReadonly());
        dto.setIsActive(role.getIsActive());
        
        Optional.ofNullable(role.getPortalType()).ifPresent(portal -> {
            dto.setPortalTypeId(portal.getPortalTypeId());
            dto.setPortalTypeName(portal.getPortalName());
        });
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
        List<PermissionDto> permissions = Optional.ofNullable(rolePermissions).orElse(Collections.emptyList()).stream()
            .map(RolePermission::getPermission)
            .filter(Objects::nonNull)
            .map(this::convertPermissionToDto)
            .filter(Objects::nonNull)
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
        dto.setIsSuperadmin(role.getIsSuperadmin());
        dto.setIsReadonly(role.getIsReadonly());
        dto.setIsActive(role.getIsActive());
        
        Optional.ofNullable(role.getPortalType())
            .ifPresent(portal -> dto.setPortalTypeName(portal.getPortalName()));
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleRoleId(role.getRoleId());
        dto.setPermissionCount(Optional.ofNullable(rolePermissions).map(List::size).orElse(0));
        
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
