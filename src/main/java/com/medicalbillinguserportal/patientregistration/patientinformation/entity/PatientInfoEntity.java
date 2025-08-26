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
    private String firstName;
    @Column(length = 30)
    private String middleName;
    @Column(length = 30)
    private String lastName;
    @Column(length = 20)
    private String suffix;
    @Column(length = 30)
    private String previousFirstName;
    @Column(length = 15)
    private LocalDate dob;
    @Column(length = 8)
    private String sex;
    @Column(length = 50)
    private String sexualOrientation;
    @Column(length = 10)
    private String genderIdentity;
    @Column(length = 30)
    private String race;
    @Column(length = 30)
    private String ethnicity;
    @Column(length = 15)
    private String language;
    @Column(length = 30)
    private String country;
    @Column(length = 50)
    private String ssn;
    @Column(length = 15)
    private String maritalStatus;
    @Column(length = 15)
    private Boolean multipleBirth;
    private Integer birthOrder;
    private String mothersMaidenName;
    private Boolean residentOfPractice;

    // Contact Info
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
    @Column(length = 30)
    private String contactPreference;
    @Column(length = 100)
    private String address1;
    @Column(length = 100)
    private String address2;
    @Column(length = 30)
    private String city;
    @Column(length = 30)
    private String state;
    @Column(length = 20)
    private String zip;

    // Emergency Contact
    @Column(length = 20)
    private String emergencyFirstName;
    @Column(length = 20)
    private String emergencyMiddleName;
    @Column(length = 20)
    private String emergencyLastName;
    @Column(length = 20)
    private String emergencySuffix;
    @Column(length = 20)
    private String emergencyPhone;
    @Column(length = 20)
    private String emergencyRelationship;

    // Caregiver
    @Column(length = 20)
    private String caregiverFirstName;
    @Column(length = 20)
    private String caregiverMiddleName;
    @Column(length = 20)
    private String caregiverLastName;
    @Column(length = 20)
    private String caregiverSuffix;
    @Column(length = 20)
    private String caregiverPhone;
    @Column(length = 20)
    private String caregiverRelationship;
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
    @Column(length = 200)
    private String caregiverComment;

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

    // Account Info
    @Column(length = 30)
    private String chartNo;
    @Column(length = 30)
    private LocalDate dateRegistered;
    @Column(length = 50)
    private String accountType;
    @Column(length = 30)
    private LocalDate dateOfFirstOccurrence;
    @Column(length = 30)
    private String accountStatus;
    @Column(length = 30)
    private String accountSecondaryStatus;

    // Referral Info
    @Column(length = 30)
    private String referralFirstName;
    @Column(length = 30)
    private String referralMiddleName;
    @Column(length = 30)
    private String referralPhone;
    @Column(length = 30)
    private String referralRelationship;
    @Column(length = 150)
    private String referralAddress1;
    @Column(length = 150)
    private String referralAddress2;
    @Column(length = 30)
    private String referralCity;
    @Column(length = 30)
    private String referralState;
    @Column(length = 30)
    private String referralZip;
    @Column(length = 200)
    private String referralComment;
    @Column(name = "complete_registration")
    private Boolean completeRegistration = false;

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
