package com.numbericsuserportal.usermanagement.domain;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "portal_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalType extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portal_type_id")
    private Long portalTypeId;
    
    @Column(name = "portal_name", nullable = false, unique = true)
    private String portalName;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Column(name = "description")
    private String description;
    
    // Constants for easy reference
    public static final Long SUPER_ADMIN_PORTAL_ID = 1L;
    public static final Long PM_PORTAL_ID = 2L;
    
    public static final String SUPER_ADMIN_PORTAL_NAME = "SUPER_ADMIN_PORTAL";
    public static final String PM_PORTAL_NAME = "PM_PORTAL";
}
