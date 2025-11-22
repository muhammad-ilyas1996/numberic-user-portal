package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {
    
    private Long permissionId;
    private String codeName;
    private String displayName;
    private String category;
    private String description;
    private Long portalTypeId;
    private String portalTypeName;
    private Boolean isSuperadmin;
    private Boolean isActive;
}
