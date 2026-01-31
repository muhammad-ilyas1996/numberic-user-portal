package com.numbericsuserportal.usermanagement.domain;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import com.numbericsuserportal.usermanagement.domain.PortalType;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;
    
    @Column(name = "code_name", nullable = false, unique = true, length = 50)
    private String codeName;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_type_id", nullable = false)
    private PortalType portalType;
    
    @Column(name = "is_superadmin", nullable = false)
    private Boolean isSuperadmin = false;
    
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    private Set<RolePermission> rolePermissions = new HashSet<>();
    
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    private Set<MenuPermission> menuPermissions = new HashSet<>();
}
