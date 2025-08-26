package com.medicalbillinguserportal.patientregistration.guarantorinfo.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patient_guarantor_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientGuarantorEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String relationToPatient;
    private String firstName;
    private String middleName;
    private String lastNameOrOrg;
    private LocalDate dob;
    private String sex;

    private String race;
    private String ethnicity;
    private String language;
    private String country;

    private String ssn;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String maritalStatus;
    private String employmentStatus;

    private String homePhone;
    private String workPhone;
    private String workPhoneExt;
    private String cellPhone;
    private String email;

    private String previousAddress1;
    private String previousAddress2;
    private String previousCity;
    private String previousState;
    private String previousZip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_info_id")
    private PatientInfoEntity patientInfoEntity;
}
