package com.medicalbillinguserportal.usermanagement.domain;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import com.medicalbillinguserportal.usermanagement.domain.PortalType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role  extends BaseEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;
    
    @Column(name = "code_name", nullable = false, length = 50)
    private String codeName;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "description", nullable = false, length = 200)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_type_id", nullable = false)
    private PortalType portalType;
    
    @Column(name = "is_superadmin", nullable = false)
    private Boolean isSuperadmin = false;
    
    @Column(name = "is_readonly", nullable = false)
    private Boolean isReadonly = false;
    
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<RolePermission> rolePermissions = new HashSet<>();
    
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<UserRole> userRoles = new HashSet<>();
}
