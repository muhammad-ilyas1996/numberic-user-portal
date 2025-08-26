package com.medicalbillinguserportal.patientregistration.patientinformation.dto;

import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.dto.PatientGuarantorDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.message.dto.PatientMessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientInfoDto {
    private Long id;

    // Patient Identification
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String previousFirstName;
    private LocalDate dob;
    private String sex;
    private String sexualOrientation;
    private String genderIdentity;
    private String race;
    private String ethnicity;
    private String language;
    private String country;
    private String ssn;
    private String maritalStatus;
    private Boolean multipleBirth;
    private Integer birthOrder;
    private String mothersMaidenName;
    private Boolean residentOfPractice;

    // Contact Info
    private String homePhone;
    private String workPhone;
    private String workPhoneExt;
    private String cellPhone;
    private String email;
    private String contactPreference;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;

    // Emergency Contact
    private String emergencyFirstName;
    private String emergencyMiddleName;
    private String emergencyLastName;
    private String emergencySuffix;
    private String emergencyPhone;
    private String emergencyRelationship;

    // Caregiver
    private String caregiverFirstName;
    private String caregiverMiddleName;
    private String caregiverLastName;
    private String caregiverSuffix;
    private String caregiverPhone;
    private String caregiverRelationship;
    private String caregiverAddress1;
    private String caregiverAddress2;
    private String caregiverCity;
    private String caregiverState;
    private String caregiverZip;
    private String caregiverComment;

    // Employment Info
    private String employmentStatus;
    private String employerName;
    private String employerPhone;
    private String employerAddress1;
    private String employerAddress2;
    private String employerCity;
    private String employerState;
    private String employerZip;

    // Account Info
    private String chartNo;
    private LocalDate dateRegistered;
    private String accountType;
    private LocalDate dateOfFirstOccurrence;
    private String accountStatus;
    private String accountSecondaryStatus;

    // Referral Info
    private String referralFirstName;
    private String referralMiddleName;
    private String referralPhone;
    private String referralRelationship;
    private String referralAddress1;
    private String referralAddress2;
    private String referralCity;
    private String referralState;
    private String referralZip;
    private String referralComment;

    //Base Entity
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;

    //Other six screens
    private List<PatientAssociationDto> patientAssociationDtoList;
    private List<PatientAuthorizationDto> patientAuthorizationDtoList;
    private List<PatientEpisodeDto> patientEpisodeDtoList;
    private List<PatientGuarantorDto> patientGuarantorDtoList;
    private List<PatientInsuranceDto> patientInsuranceDtoList;
    private List<PatientMessageDto> patientMessageDtoList;
}
