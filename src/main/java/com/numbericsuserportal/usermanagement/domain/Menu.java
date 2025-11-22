package com.numbericsuserportal.usermanagement.domain;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;
    
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "menu_url", length = 200)
    private String menuUrl;
    
    @Column(name = "parent_menu_id")
    private Long parentMenuId;
    
    @Column(name = "menu_order", nullable = false)
    private Integer menuOrder;
    
    @Column(name = "icon_class", length = 100)
    private String iconClass;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_type_id", nullable = false)
    private PortalType portalType;
    
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private Set<MenuPermission> menuPermissions = new HashSet<>();
}
