package com.numbericsuserportal.usermanagement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menu_permissions")
public class MenuPermission{
    
    @EmbeddedId
    private MenuPermissionId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("menuId")
    @JoinColumn(name = "menu_id")
    private Menu menu;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

}


