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
    private String patientFirstName;
    private String patientMiddleName;
    private String patientLastName;
    private String patientSuffix;
    private String patientPreviousFirstName;
    private String patientPreviousLastName;
    private LocalDate patientDob;
    private String patientSex;
    private String patientSexualOrientation;
    private String patientGenderIdentity;
    private String patientRace;
    private String patientEthnicity;
    private String patientLanguage;
    private String patientCountry;
    private String patientSsn;
    private String patientAddress1;
    private String patientAddress2;
    private String patientCity;
    private String patientState;
    private String patientZip;
    private String patientMaritalStatus;
    private String patientPregnant;

    // Employment Info
    private String employmentStatus;
    private String employerName;
    private String employerPhone;
    private String employerAddress1;
    private String employerAddress2;
    private String employerCity;
    private String employerState;
    private String employerZip;
    private String occupation;
    private Boolean multipleBirth;
    private Integer birthOrder;
    private String mothersMaidenName;

    // Previous Address
    private String previousAddress1;
    private String previousAddress2;
    private String previouscity;
    private String previousstate;
    private String previouszip;

    // Account Info
    private String chartNo;
    private LocalDate dateRegistered;
    private String accountType;
    private String accountStatus;
    private String accountSecondaryStatus;
    private String accountSignature;
    private String accountBalanceBilling;

    // Patient Contact Info
    private String homePhone;
    private String workPhone;
    private String workPhoneExt;
    private String cellPhone;
    private String email;
    private String checkboxEmail;
    private String checkboxTextMessage;
    private String contactPreference;

    // Emergency Contact
    private String emergencyFirstName;
    private String emergencyMiddleName;
    private String emergencyLastName;
    private String emergencyPhone;
    private String emergencyRelationToPatient;
    private String emergencyAddress1;
    private String emergencyAddress2;
    private String emergencyCity;
    private String emergencyState;
    private String emergencyZip;

    // Caregiver
    private String caregiverFirstName;
    private String caregiverMiddleName;
    private String caregiverLastName;
    private String caregiverRelationShip;
    private String caregiverPhone;
    private String caregiverAddress1;
    private String caregiverAddress2;
    private String caregiverCity;
    private String caregiverState;
    private String caregiverZip;
    private String caregiverIsNext;
    private String caregiverComment;

    // Legal Guardian
    private String guardianFirstName;
    private String guardianMiddleName;
    private String guardianLastName;
    private String guardianRelationship;
    private String guardianPhone;
    private String guardianAddress1;
    private String guardianAddress2;
    private String guardianCity;
    private String guardianState;
    private String guardianZip;
    private String guardianIsNext;
    private String guardianComment;

    private Boolean completeRegistration = false;

    // HealthCare
    private String healthCareFirstName;
    private String healthCareMiddleName;
    private String healthCareLastName;
    private String healthCareRelationship;
    private String healthCarePhone;
    private String healthCareAddress1;
    private String healthCareAddress2;
    private String healthCareCity;
    private String healthCareState;
    private String healthCareZip;
    private String healthCareIsNext;
    private String healthCareComment;

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
