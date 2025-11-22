package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleCreateDto {
    
    private String roleName;
    
    private String displayName;
    
    private String description;
    
    private Long portalTypeId;
    
    private Boolean isSuperadmin = false;
    
    private Boolean isReadonly = false;
    
    private List<Long> permissionIds;
}
