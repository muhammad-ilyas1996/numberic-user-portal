package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleListDto {
    
    private Long roleId;
    private String roleName;
    private String displayName;
    private String description;
    private String portalTypeName;
    private Boolean isSuperadmin;
    private Boolean isReadonly;
    private Integer permissionCount;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdOn;
}
