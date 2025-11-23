package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.usermanagement.domain.Permission;
import com.numbericsuserportal.usermanagement.domain.PortalType;
import com.numbericsuserportal.usermanagement.dto.PermissionDto;
import com.numbericsuserportal.usermanagement.repo.PermissionRepository;
import com.numbericsuserportal.usermanagement.repo.PortalTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;
    
    public List<PermissionDto> getActivePermissions() {
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndIsActiveTrue(numbricsPortal);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<PermissionDto> getPermissionsByCategory(String category) {
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndCategoryAndIsActiveTrue(numbricsPortal, category);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<String> getPermissionCategories() {
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
            
        List<Permission> permissions = permissionRepository.findByPortalTypeAndIsActiveTrue(numbricsPortal);
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
    
    public boolean existsByCodeName(String codeName) {
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
        return permissionRepository.existsByCodeNameAndPortalType(codeName, numbricsPortal);
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
        
        // Set NUMBRICS portal type
        PortalType numbricsPortal = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
        permission.setPortalType(numbricsPortal);
        
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
