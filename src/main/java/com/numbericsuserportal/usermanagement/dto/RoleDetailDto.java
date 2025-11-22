package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDetailDto {
    
    private Long roleId;
    private String roleName;
    private String displayName;
    private String description;
    private Long portalTypeId;
    private String portalTypeName;
    private Boolean isSuperadmin;
    private Boolean isReadonly;
    private List<PermissionDto> permissions;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdOn;
    private String modifiedBy;
    private LocalDateTime modifiedOn;
}
