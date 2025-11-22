package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    
    private Long roleId;
    private String codeName;
    private String displayName;
    private String description;
    private Long portalTypeId;
    private String portalTypeName;
    private Boolean isSuperadmin;
    private Boolean isActive;
    private Boolean isReadonly;
    
    // For permissions associated with this role
    private List<PermissionDto> permissions;
}
