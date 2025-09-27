package com.medicalbillinguserportal.patientregistration.patientinformation.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.entity.PatientGuarantorEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.message.entity.PatientMessageEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="patient_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfoEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Patient Identification
    @Column(length = 30)
    private String patientFirstName;
    @Column(length = 30)
    private String patientMiddleName;
    @Column(length = 30)
    private String patientLastName;
    @Column(length = 20)
    private String patientSuffix;
    @Column(length = 30)
    private String patientPreviousFirstName;
    @Column(length = 30)
    private String patientPreviousLastName;
    @Column(length = 15)
    private LocalDate patientDob;
    @Column(length = 8)
    private String patientSex;
    @Column(length = 50)
    private String patientSexualOrientation;
    @Column(length = 10)
    private String patientGenderIdentity;
    @Column(length = 30)
    private String patientRace;
    @Column(length = 30)
    private String patientEthnicity;
    @Column(length = 15)
    private String patientLanguage;
    @Column(length = 30)
    private String patientCountry;
    @Column(length = 50)
    private String patientSsn;
    @Column(length = 150)
    private String patientAddress1;
    @Column(length = 150)
    private String patientAddress2;
    @Column(length = 100)
    private String patientCity;
    @Column(length = 100)
    private String patientState;
    @Column(length = 100)
    private String patientZip;
    @Column(length = 15)
    private String patientMaritalStatus;
    @Column(length = 10)
    private String patientPregnant;
    // Employment Info
    @Column(length = 20)
    private String employmentStatus;
    @Column(length = 30)
    private String employerName;
    @Column(length = 30)
    private String employerPhone;
    @Column(length = 150)
    private String employerAddress1;
    @Column(length = 150)
    private String employerAddress2;
    @Column(length = 30)
    private String employerCity;
    @Column(length = 30)
    private String employerState;
    @Column(length = 30)
    private String employerZip;
    @Column(length = 100)
    private String Occupation;
    @Column(length = 15)
    private Boolean multipleBirth;
    @Column(length = 10)
    private Integer birthOrder;
    @Column(length = 100)
    private String mothersMaidenName;

    //Previous Address
    @Column(length = 100)
    private String previousAddress1;
    @Column(length = 100)
    private String previousAddress2;
    @Column(length = 30)
    private String previouscity;
    @Column(length = 30)
    private String previousstate;
    @Column(length = 20)
    private String previouszip;

// Account Info
    @Column(length = 30)
    private String chartNo;
    @Column(length = 30)
    private LocalDate dateRegistered;
    @Column(length = 50)
    private String accountType;
    @Column(length = 30)
    private String accountStatus;
    @Column(length = 30)
    private String accountSecondaryStatus;
    @Column(length = 30)
    private String accountSignature;
    @Column(length = 30)
    private String accountBalanceBilling;

    //Patient Contact Info
    @Column(length = 15)
    private String homePhone;
    @Column(length = 15)
    private String workPhone;
    @Column(length = 15)
    private String workPhoneExt;
    @Column(length = 15)
    private String cellPhone;
    @Column(length = 40)
    private String email;
    @Column(length = 10)
    private String checkboxEmail;
    @Column(length = 10)
    private String checkboxTextMessage;
    @Column(length = 30)
    private String contactPreference;

    // Emergency Contact
    @Column(length = 20)
    private String emergencyFirstName;
    @Column(length = 20)
    private String emergencyMiddleName;
    @Column(length = 20)
    private String emergencyLastName;
    @Column(length = 20)
    private String emergencyPhone;
    @Column(length = 20)
    private String emergencyRelationToPatient;
    @Column(length = 20)
    private String emergencyAddress1;
    @Column(length = 20)
    private String emergencyAddress2;
    @Column(length = 20)
    private String emergencyCity;
    @Column(length = 20)
    private String emergencyState;
    @Column(length = 20)
    private String emergencyZip;

    // Caregiver
    @Column(length = 20)
    private String caregiverFirstName;
    @Column(length = 20)
    private String caregiverMiddleName;
    @Column(length = 20)
    private String caregiverLastName;
    @Column(length = 20)
    private String caregiverRelationShip;
    @Column(length = 20)
    private String caregiverPhone;
    @Column(length = 100)
    private String caregiverAddress1;
    @Column(length = 100)
    private String caregiverAddress2;
    @Column(length = 30)
    private String caregiverCity;
    @Column(length = 30)
    private String caregiverState;
    @Column(length = 30)
    private String caregiverZip;
    @Column(length = 10)
    private String caregiverIsNext;
    @Column(length = 300)
    private String caregiverComment;


//Legal Guardian
    @Column(length = 30)
    private String guardianFirstName;
    @Column(length = 30)
    private String guardianMiddleName;
    @Column(length = 30)
    private String guardianLastName;
    @Column(length = 30)
    private String guardianRelationship;
    @Column(length = 30)
    private String guardianPhone;
    @Column(length = 150)
    private String guardianAddress1;
    @Column(length = 150)
    private String guardianAddress2;
    @Column(length = 30)
    private String guardianCity;
    @Column(length = 30)
    private String guardianState;
    @Column(length = 30)
    private String guardianZip;
    @Column(length = 10)
    private String guardianIsNext;
    @Column(length = 250)
    private String guardianComment;

    @Column(name = "complete_registration")
    private Boolean completeRegistration = false;

    //healthCare
    @Column(length = 30)
    private String healthCareFirstName;
    @Column(length = 30)
    private String healthCareMiddleName;
    @Column(length = 30)
    private String healthCareLastName;
    @Column(length = 30)
    private String healthCareRelationship;
    @Column(length = 30)
    private String healthCarePhone;
    @Column(length = 150)
    private String healthCareAddress1;
    @Column(length = 150)
    private String healthCareAddress2;
    @Column(length = 30)
    private String healthCareCity;
    @Column(length = 30)
    private String healthCareState;
    @Column(length = 30)
    private String healthCareZip;
    @Column(length = 10)
    private String healthCareIsNext;
    @Column(length = 250)
    private String healthCareComment;


    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientAssociationEntity> patientAssociationList = new ArrayList<>();;
    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientAuthorizationEntity> patientAuthorizationEntitiesList = new ArrayList<>();;
    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientEpisodeEntity> patientEpisodeEntityList= new ArrayList<>();;
    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientGuarantorEntity> patientGuarantorEntityList= new ArrayList<>();;
    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientInsuranceEntity> patientInsuranceEntityList= new ArrayList<>();;
    @OneToMany(mappedBy = "patientInfoEntity" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PatientMessageEntity> patientMessageEntityList= new ArrayList<>();;


}
