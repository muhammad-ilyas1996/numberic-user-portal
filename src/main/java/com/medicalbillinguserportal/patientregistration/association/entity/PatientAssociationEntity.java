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
    private String referringProvider;
    private String priorAuthorization;
    private String otherReferralSource;

    // PCP Information
    private String defaultPCP;
    private LocalDate lastSeenByPCP;

    // Default Pharmacy Information
    private String defaultPharmacy;
    private String pharmacyPhoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
