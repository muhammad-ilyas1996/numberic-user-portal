package com.numbericsuserportal.commonpersistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@MappedSuperclass
public class BaseEntity {

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_ON", nullable = false)
    private Date createdOn;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(name = "MODIFIED_ON")
    private Date modifiedOn;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive = true;

    @PrePersist
    protected void prePersist() {
        if( createdBy==null ) {
            createdBy = modifiedBy = "UNKNOWN";
        } else {
            modifiedBy = createdBy;
        }
        if( createdOn==null ) {
            createdOn = modifiedOn = new Date();
        } else {
            modifiedOn = createdOn;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        if( modifiedBy==null ) {
            modifiedBy = "UNKNOWN";
        }
        if( modifiedOn==null ) {
            modifiedOn = new Date();
        }
    }

}
