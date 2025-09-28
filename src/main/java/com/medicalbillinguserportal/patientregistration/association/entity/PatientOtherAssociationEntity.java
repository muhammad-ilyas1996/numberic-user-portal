package com.medicalbillinguserportal.patientregistration.association.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "other_association")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientOtherAssociationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String phone;
    private String ext;
    private String fax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_association_id")
    private PatientAssociationEntity patientAssociation;
}
