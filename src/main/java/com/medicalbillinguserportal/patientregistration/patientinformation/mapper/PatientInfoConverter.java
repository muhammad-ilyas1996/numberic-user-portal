package com.medicalbillinguserportal.patientregistration.patientinformation.mapper;

import com.medicalbillinguserportal.patientregistration.association.mapper.PatientAssociationConverter;
import com.medicalbillinguserportal.patientregistration.authorization.mapper.PatientAuthorizationConverter;
import com.medicalbillinguserportal.patientregistration.episode.mapper.PatientEpisodeConverter;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.mapper.PatientGuarantorConverter;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.mapper.PatientInsuranceConverter;
import com.medicalbillinguserportal.patientregistration.message.mapper.PatientMessageConverter;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;
import java.util.stream.Collectors;

public class PatientInfoConverter {

    public static PatientInfoDto toDto(PatientInfoEntity entity, User currentUser)
    {
        if(entity ==null)
        {
            return null;
        }
        PatientInfoDto dto=new PatientInfoDto();
        dto.setId(entity.getId());
        dto.setPatientFirstName(entity.getPatientFirstName());
        dto.setPatientMiddleName(entity.getPatientMiddleName());
        dto.setPatientLastName(entity.getPatientLastName());
        dto.setPatientSuffix(entity.getPatientSuffix());
        dto.setPatientPreviousFirstName(entity.getPatientPreviousFirstName());
        dto.setPatientPreviousLastName(entity.getPatientPreviousLastName());
        dto.setPatientDob(entity.getPatientDob());
        dto.setPatientSex(entity.getPatientSex());
        dto.setPatientSexualOrientation(entity.getPatientSexualOrientation());
        dto.setPatientGenderIdentity(entity.getPatientGenderIdentity());
        dto.setPatientRace(entity.getPatientRace());
        dto.setPatientEthnicity(entity.getPatientEthnicity());
        dto.setPatientLanguage(entity.getPatientLanguage());
        dto.setPatientCountry(entity.getPatientCountry());
        dto.setPatientSsn(entity.getPatientSsn());
        dto.setPatientAddress1(entity.getPatientAddress1());
        dto.setPatientAddress2(entity.getPatientAddress2());
        dto.setPatientCity(entity.getPatientCity());
        dto.setPatientState(entity.getPatientState());
        dto.setPatientZip(entity.getPatientZip());
        dto.setPatientMaritalStatus(entity.getPatientMaritalStatus());
        dto.setPatientPregnant(entity.getPatientPregnant());

        // Employment Info
        dto.setEmploymentStatus(entity.getEmploymentStatus());
        dto.setEmployerName(entity.getEmployerName());
        dto.setEmployerPhone(entity.getEmployerPhone());
        dto.setEmployerAddress1(entity.getEmployerAddress1());
        dto.setEmployerAddress2(entity.getEmployerAddress2());
        dto.setEmployerCity(entity.getEmployerCity());
        dto.setEmployerState(entity.getEmployerState());
        dto.setEmployerZip(entity.getEmployerZip());
        dto.setOccupation(entity.getOccupation());
        dto.setMultipleBirth(entity.getMultipleBirth());
        dto.setBirthOrder(entity.getBirthOrder());
        dto.setMothersMaidenName(entity.getMothersMaidenName());

        // Previous Address
        dto.setPreviousAddress1(entity.getPreviousAddress1());
        dto.setPreviousAddress2(entity.getPreviousAddress2());
        dto.setPreviouscity(entity.getPreviouscity());
        dto.setPreviousstate(entity.getPreviousstate());
        dto.setPreviouszip(entity.getPreviouszip());

        // Account Info
        dto.setChartNo(entity.getChartNo());
        dto.setDateRegistered(entity.getDateRegistered());
        dto.setAccountType(entity.getAccountType());
        dto.setAccountStatus(entity.getAccountStatus());
        dto.setAccountSecondaryStatus(entity.getAccountSecondaryStatus());
        dto.setAccountSignature(entity.getAccountSignature());
        dto.setAccountBalanceBilling(entity.getAccountBalanceBilling());

        // Contact Info
        dto.setHomePhone(entity.getHomePhone());
        dto.setWorkPhone(entity.getWorkPhone());
        dto.setWorkPhoneExt(entity.getWorkPhoneExt());
        dto.setCellPhone(entity.getCellPhone());
        dto.setEmail(entity.getEmail());
        dto.setCheckboxEmail(entity.getCheckboxEmail());
        dto.setCheckboxTextMessage(entity.getCheckboxTextMessage());
        dto.setContactPreference(entity.getContactPreference());

        // Emergency Contact
        dto.setEmergencyFirstName(entity.getEmergencyFirstName());
        dto.setEmergencyMiddleName(entity.getEmergencyMiddleName());
        dto.setEmergencyLastName(entity.getEmergencyLastName());
        dto.setEmergencyPhone(entity.getEmergencyPhone());
        dto.setEmergencyRelationToPatient(entity.getEmergencyRelationToPatient());
        dto.setEmergencyAddress1(entity.getEmergencyAddress1());
        dto.setEmergencyAddress2(entity.getEmergencyAddress2());
        dto.setEmergencyCity(entity.getEmergencyCity());
        dto.setEmergencyState(entity.getEmergencyState());
        dto.setEmergencyZip(entity.getEmergencyZip());

        // Caregiver
        dto.setCaregiverFirstName(entity.getCaregiverFirstName());
        dto.setCaregiverMiddleName(entity.getCaregiverMiddleName());
        dto.setCaregiverLastName(entity.getCaregiverLastName());
        dto.setCaregiverRelationShip(entity.getCaregiverRelationShip());
        dto.setCaregiverPhone(entity.getCaregiverPhone());
        dto.setCaregiverAddress1(entity.getCaregiverAddress1());
        dto.setCaregiverAddress2(entity.getCaregiverAddress2());
        dto.setCaregiverCity(entity.getCaregiverCity());
        dto.setCaregiverState(entity.getCaregiverState());
        dto.setCaregiverZip(entity.getCaregiverZip());
        dto.setCaregiverIsNext(entity.getCaregiverIsNext());
        dto.setCaregiverComment(entity.getCaregiverComment());

        // Guardian
        dto.setGuardianFirstName(entity.getGuardianFirstName());
        dto.setGuardianMiddleName(entity.getGuardianMiddleName());
        dto.setGuardianLastName(entity.getGuardianLastName());
        dto.setGuardianRelationship(entity.getGuardianRelationship());
        dto.setGuardianPhone(entity.getGuardianPhone());
        dto.setGuardianAddress1(entity.getGuardianAddress1());
        dto.setGuardianAddress2(entity.getGuardianAddress2());
        dto.setGuardianCity(entity.getGuardianCity());
        dto.setGuardianState(entity.getGuardianState());
        dto.setGuardianZip(entity.getGuardianZip());
        dto.setGuardianIsNext(entity.getGuardianIsNext());
        dto.setGuardianComment(entity.getGuardianComment());

        dto.setCompleteRegistration(entity.getCompleteRegistration());

        // Health Care
        dto.setHealthCareFirstName(entity.getHealthCareFirstName());
        dto.setHealthCareMiddleName(entity.getHealthCareMiddleName());
        dto.setHealthCareLastName(entity.getHealthCareLastName());
        dto.setHealthCareRelationship(entity.getHealthCareRelationship());
        dto.setHealthCarePhone(entity.getHealthCarePhone());
        dto.setHealthCareAddress1(entity.getHealthCareAddress1());
        dto.setHealthCareAddress2(entity.getHealthCareAddress2());
        dto.setHealthCareCity(entity.getHealthCareCity());
        dto.setHealthCareState(entity.getHealthCareState());
        dto.setHealthCareZip(entity.getHealthCareZip());
        dto.setHealthCareIsNext(entity.getHealthCareIsNext());
        dto.setHealthCareComment(entity.getHealthCareComment());


        if (currentUser != null) {
            // Auditing fields
            dto.setCreatedBy(currentUser.getUserId().toString());
            dto.setModifiedBy(currentUser.getUserId().toString());
        }
        dto.setCreatedOn(new Date());
        dto.setModifiedOn(new Date());

        dto.setPatientAssociationDtoList(
                entity.getPatientAssociationList().stream()
                        .map(PatientAssociationConverter::toDTO)
                        .collect(Collectors.toList())
        );
        dto.setPatientAuthorizationDtoList(
                entity.getPatientAuthorizationEntitiesList().stream()
                        .map(PatientAuthorizationConverter::toDTO)
                        .collect(Collectors.toList())
        );
        dto.setPatientEpisodeDtoList(
                entity.getPatientEpisodeEntityList().stream()
                        .map(PatientEpisodeConverter::toDTO)
                        .collect(Collectors.toList())
        );
        dto.setPatientGuarantorDtoList(
                entity.getPatientGuarantorEntityList().stream()
                        .map(PatientGuarantorConverter::toDto)
                        .collect(Collectors.toList())

        );
        dto.setPatientInsuranceDtoList(
                entity.getPatientInsuranceEntityList().stream()
                        .map(PatientInsuranceConverter::toDto)
                        .collect(Collectors.toList())
        );
        dto.setPatientMessageDtoList(
                entity.getPatientMessageEntityList().stream()
                        .map(PatientMessageConverter::toDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    public static PatientInfoEntity toEntity(PatientInfoDto dto, User currentUser) {
        if (dto == null) return null;

        PatientInfoEntity entity = new PatientInfoEntity();

        entity.setId(dto.getId());
        entity.setPatientFirstName(dto.getPatientFirstName());
        entity.setPatientMiddleName(dto.getPatientMiddleName());
        entity.setPatientLastName(dto.getPatientLastName());
        entity.setPatientSuffix(dto.getPatientSuffix());
        entity.setPatientPreviousFirstName(dto.getPatientPreviousFirstName());
        entity.setPatientPreviousLastName(dto.getPatientPreviousLastName());
        entity.setPatientDob(dto.getPatientDob());
        entity.setPatientSex(dto.getPatientSex());
        entity.setPatientSexualOrientation(dto.getPatientSexualOrientation());
        entity.setPatientGenderIdentity(dto.getPatientGenderIdentity());
        entity.setPatientRace(dto.getPatientRace());
        entity.setPatientEthnicity(dto.getPatientEthnicity());
        entity.setPatientLanguage(dto.getPatientLanguage());
        entity.setPatientCountry(dto.getPatientCountry());
        entity.setPatientSsn(dto.getPatientSsn());
        entity.setPatientAddress1(dto.getPatientAddress1());
        entity.setPatientAddress2(dto.getPatientAddress2());
        entity.setPatientCity(dto.getPatientCity());
        entity.setPatientState(dto.getPatientState());
        entity.setPatientZip(dto.getPatientZip());
        entity.setPatientMaritalStatus(dto.getPatientMaritalStatus());
        entity.setPatientPregnant(dto.getPatientPregnant());

        // Employment Info
        entity.setEmploymentStatus(dto.getEmploymentStatus());
        entity.setEmployerName(dto.getEmployerName());
        entity.setEmployerPhone(dto.getEmployerPhone());
        entity.setEmployerAddress1(dto.getEmployerAddress1());
        entity.setEmployerAddress2(dto.getEmployerAddress2());
        entity.setEmployerCity(dto.getEmployerCity());
        entity.setEmployerState(dto.getEmployerState());
        entity.setEmployerZip(dto.getEmployerZip());
        entity.setOccupation(dto.getOccupation());
        entity.setMultipleBirth(dto.getMultipleBirth());
        entity.setBirthOrder(dto.getBirthOrder());
        entity.setMothersMaidenName(dto.getMothersMaidenName());

        // Previous Address
        entity.setPreviousAddress1(dto.getPreviousAddress1());
        entity.setPreviousAddress2(dto.getPreviousAddress2());
        entity.setPreviouscity(dto.getPreviouscity());
        entity.setPreviousstate(dto.getPreviousstate());
        entity.setPreviouszip(dto.getPreviouszip());

        // Account Info
        entity.setChartNo(dto.getChartNo());
        entity.setDateRegistered(dto.getDateRegistered());
        entity.setAccountType(dto.getAccountType());
        entity.setAccountStatus(dto.getAccountStatus());
        entity.setAccountSecondaryStatus(dto.getAccountSecondaryStatus());
        entity.setAccountSignature(dto.getAccountSignature());
        entity.setAccountBalanceBilling(dto.getAccountBalanceBilling());

        // Contact Info
        entity.setHomePhone(dto.getHomePhone());
        entity.setWorkPhone(dto.getWorkPhone());
        entity.setWorkPhoneExt(dto.getWorkPhoneExt());
        entity.setCellPhone(dto.getCellPhone());
        entity.setEmail(dto.getEmail());
        entity.setCheckboxEmail(dto.getCheckboxEmail());
        entity.setCheckboxTextMessage(dto.getCheckboxTextMessage());
        entity.setContactPreference(dto.getContactPreference());

        // Emergency Contact
        entity.setEmergencyFirstName(dto.getEmergencyFirstName());
        entity.setEmergencyMiddleName(dto.getEmergencyMiddleName());
        entity.setEmergencyLastName(dto.getEmergencyLastName());
        entity.setEmergencyPhone(dto.getEmergencyPhone());
        entity.setEmergencyRelationToPatient(dto.getEmergencyRelationToPatient());
        entity.setEmergencyAddress1(dto.getEmergencyAddress1());
        entity.setEmergencyAddress2(dto.getEmergencyAddress2());
        entity.setEmergencyCity(dto.getEmergencyCity());
        entity.setEmergencyState(dto.getEmergencyState());
        entity.setEmergencyZip(dto.getEmergencyZip());

        // Caregiver
        entity.setCaregiverFirstName(dto.getCaregiverFirstName());
        entity.setCaregiverMiddleName(dto.getCaregiverMiddleName());
        entity.setCaregiverLastName(dto.getCaregiverLastName());
        entity.setCaregiverRelationShip(dto.getCaregiverRelationShip());
        entity.setCaregiverPhone(dto.getCaregiverPhone());
        entity.setCaregiverAddress1(dto.getCaregiverAddress1());
        entity.setCaregiverAddress2(dto.getCaregiverAddress2());
        entity.setCaregiverCity(dto.getCaregiverCity());
        entity.setCaregiverState(dto.getCaregiverState());
        entity.setCaregiverZip(dto.getCaregiverZip());
        entity.setCaregiverIsNext(dto.getCaregiverIsNext());
        entity.setCaregiverComment(dto.getCaregiverComment());

        // Guardian
        entity.setGuardianFirstName(dto.getGuardianFirstName());
        entity.setGuardianMiddleName(dto.getGuardianMiddleName());
        entity.setGuardianLastName(dto.getGuardianLastName());
        entity.setGuardianRelationship(dto.getGuardianRelationship());
        entity.setGuardianPhone(dto.getGuardianPhone());
        entity.setGuardianAddress1(dto.getGuardianAddress1());
        entity.setGuardianAddress2(dto.getGuardianAddress2());
        entity.setGuardianCity(dto.getGuardianCity());
        entity.setGuardianState(dto.getGuardianState());
        entity.setGuardianZip(dto.getGuardianZip());
        entity.setGuardianIsNext(dto.getGuardianIsNext());
        entity.setGuardianComment(dto.getGuardianComment());

        entity.setCompleteRegistration(dto.getCompleteRegistration());

        // Health Care
        entity.setHealthCareFirstName(dto.getHealthCareFirstName());
        entity.setHealthCareMiddleName(dto.getHealthCareMiddleName());
        entity.setHealthCareLastName(dto.getHealthCareLastName());
        entity.setHealthCareRelationship(dto.getHealthCareRelationship());
        entity.setHealthCarePhone(dto.getHealthCarePhone());
        entity.setHealthCareAddress1(dto.getHealthCareAddress1());
        entity.setHealthCareAddress2(dto.getHealthCareAddress2());
        entity.setHealthCareCity(dto.getHealthCareCity());
        entity.setHealthCareState(dto.getHealthCareState());
        entity.setHealthCareZip(dto.getHealthCareZip());
        entity.setHealthCareIsNext(dto.getHealthCareIsNext());
        entity.setHealthCareComment(dto.getHealthCareComment());


        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }
}
