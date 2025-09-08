package com.medicalbillinguserportal.usermanagement.service;

import com.medicalbillinguserportal.usermanagement.domain.Permission;
import com.medicalbillinguserportal.usermanagement.domain.PortalType;
import com.medicalbillinguserportal.usermanagement.dto.PermissionDto;
import com.medicalbillinguserportal.usermanagement.repo.PermissionRepository;
import com.medicalbillinguserportal.usermanagement.repo.PortalTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;
    
    public List<PermissionDto> getActivePermissionsByPortalType(Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndIsActiveTrue(portalType);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<PermissionDto> getPermissionsByCategoryAndPortalType(String category, Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndCategoryAndIsActiveTrue(portalType, category);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<String> getPermissionCategoriesByPortalType(Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndIsActiveTrue(portalType);
        return permissions.stream()
                .map(Permission::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
    
    public PermissionDto createPermission(PermissionDto permissionDto) {
        Permission permission = convertToEntity(permissionDto);
        // BaseEntity automatically manages these fields
        // No need to set them manually
        
        Permission savedPermission = permissionRepository.save(permission);
        return convertToDto(savedPermission);
    }
    
    public PermissionDto updatePermission(PermissionDto permissionDto) {
        Optional<Permission> existingPermission = permissionRepository.findById(permissionDto.getPermissionId());
        if (existingPermission.isPresent()) {
            Permission permission = existingPermission.get();
            updatePermissionFromDto(permission, permissionDto);
            // BaseEntity automatically manages these fields
            // No need to set them manually
            
            Permission savedPermission = permissionRepository.save(permission);
            return convertToDto(savedPermission);
        }
        throw new RuntimeException("Permission not found with id: " + permissionDto.getPermissionId());
    }
    
    public void deletePermission(Long permissionId) {
        Optional<Permission> permission = permissionRepository.findById(permissionId);
        if (permission.isPresent()) {
            Permission existingPermission = permission.get();
            existingPermission.setIsActive(false);
            // BaseEntity automatically manages these fields
            // No need to set them manually
            permissionRepository.save(existingPermission);
        }
    }
    
    public boolean existsByCodeNameAndPortalType(String codeName, Long portalTypeId) {
        PortalType portalType = portalTypeRepository.findById(portalTypeId)
            .orElseThrow(() -> new RuntimeException("Portal type not found"));
        return permissionRepository.existsByCodeNameAndPortalType(codeName, portalType);
    }
    
    private PermissionDto convertToDto(Permission permission) {
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
        // No need to set them in DTO
        return dto;
    }
    
    private Permission convertToEntity(PermissionDto dto) {
        Permission permission = new Permission();
        permission.setCodeName(dto.getCodeName());
        permission.setDisplayName(dto.getDisplayName());
        permission.setCategory(dto.getCategory());
        permission.setDescription(dto.getDescription());
        
        // Set portal type from ID
        if (dto.getPortalTypeId() != null) {
            PortalType portalType = portalTypeRepository.findById(dto.getPortalTypeId())
                .orElseThrow(() -> new RuntimeException("Portal type not found"));
            permission.setPortalType(portalType);
        }
        
        permission.setIsSuperadmin(dto.getIsSuperadmin());
        permission.setIsActive(dto.getIsActive());
        return permission;
    }
    
    private void updatePermissionFromDto(Permission permission, PermissionDto dto) {
        if (dto.getDisplayName() != null) permission.setDisplayName(dto.getDisplayName());
        if (dto.getCategory() != null) permission.setCategory(dto.getCategory());
        if (dto.getDescription() != null) permission.setDescription(dto.getDescription());
        if (dto.getIsSuperadmin() != null) permission.setIsSuperadmin(dto.getIsSuperadmin());
        if (dto.getIsActive() != null) permission.setIsActive(dto.getIsActive());
    }
}
