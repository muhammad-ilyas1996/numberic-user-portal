package com.numbericsuserportal.usermanagement.domain;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "portal_types")
@Data
@EqualsAndHashCode(callSuper = false)
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
    
    // Constants for NUMBRICS Portal (single portal)
    public static final String NUMBRICS_PORTAL_NAME = "NUMBRICS_PORTAL";
}
