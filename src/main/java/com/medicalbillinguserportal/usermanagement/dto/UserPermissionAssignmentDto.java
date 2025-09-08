package com.medicalbillinguserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionAssignmentDto {
    
    private Long userId;
    private Long roleId;
    private List<Long> permissionIds;
    private String portalType; // ADMIN_PORTAL or PM_PORTAL
}


