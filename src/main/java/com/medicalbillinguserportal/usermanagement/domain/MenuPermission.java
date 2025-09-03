package com.medicalbillinguserportal.usermanagement.domain;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menu_permissions")
public class MenuPermission extends BaseEntity {
    
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
    
//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive = true;
    
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @Column(name = "created_by")
//    private Long createdBy;
//
//    @Column(name = "updated_by")
//    private Long updatedBy;
}


