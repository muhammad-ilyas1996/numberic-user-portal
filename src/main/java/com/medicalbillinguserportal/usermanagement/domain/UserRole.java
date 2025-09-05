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
@Table(name = "user_roles")
public class UserRole {
    
    @EmbeddedId
    private UserRoleId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    // Add missing isActive field
    @Column(name = "is_active")
    private Boolean isActive = true;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "added_by", nullable = false)
    private Long addedBy;

}


