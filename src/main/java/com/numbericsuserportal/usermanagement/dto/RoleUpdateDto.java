package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateDto {
    
    @NotNull(message = "Role ID is required")
    private Long roleId;
    
    @NotBlank(message = "Role name is required")
    private String roleName;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String description;
    
    private Boolean isSuperadmin = false;
    
    private Boolean isReadonly = false;
    
    @NotNull(message = "Permission IDs are required")
    private List<Long> permissionIds;
}
