package com.numbericsuserportal.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuDto {
    
    private Long menuId;
    private String menuName;
    private String displayName;
    private String menuUrl;
    private Long parentMenuId;
    private Integer menuOrder;
    private String iconClass;
    private Boolean isActive;
    private Long portalTypeId;
    private String portalTypeName;
    
    // For hierarchical menu structure
    private List<MenuDto> subMenus;
    
    // For permissions associated with this menu
    private List<PermissionDto> permissions;
}
