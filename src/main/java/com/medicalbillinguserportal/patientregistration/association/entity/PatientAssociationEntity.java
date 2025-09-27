package com.medicalbillinguserportal.patientregistration.association.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patient_association")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAssociationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referring Provider Information
    private String associationProvider;
    private String associationReferringProvider;
    private String associationPriorAuthorization;
    private String associationOtherReferralSource;

    // PCP Information
    private String patientOutsidePCP;
    private LocalDate lastSeenByPCP;

//New Association
    private String associationFirstName;
    private String associationLastName;
    private String associationRole;
    private String associationEmail;
    private String associationPhone;
    private String associationExt;
    private String associationFax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
