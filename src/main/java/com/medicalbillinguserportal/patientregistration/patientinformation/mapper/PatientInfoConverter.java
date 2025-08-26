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
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setSuffix(entity.getSuffix());
        dto.setPreviousFirstName(entity.getPreviousFirstName());
        dto.setDob(entity.getDob());
        dto.setSex(entity.getSex());
        dto.setSexualOrientation(entity.getSexualOrientation());
        dto.setGenderIdentity(entity.getGenderIdentity());
        dto.setRace(entity.getRace());
        dto.setEthnicity(entity.getEthnicity());
        dto.setLanguage(entity.getLanguage());
        dto.setCountry(entity.getCountry());
        dto.setSsn(entity.getSsn());
        dto.setMaritalStatus(entity.getMaritalStatus());
        dto.setMultipleBirth(entity.getMultipleBirth());
        dto.setBirthOrder(entity.getBirthOrder());
        dto.setMothersMaidenName(entity.getMothersMaidenName());
        dto.setResidentOfPractice(entity.getResidentOfPractice());

        dto.setHomePhone(entity.getHomePhone());
        dto.setWorkPhone(entity.getWorkPhone());
        dto.setWorkPhoneExt(entity.getWorkPhoneExt());
        dto.setCellPhone(entity.getCellPhone());
        dto.setEmail(entity.getEmail());
        dto.setContactPreference(entity.getContactPreference());
        dto.setAddress1(entity.getAddress1());
        dto.setAddress2(entity.getAddress2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setZip(entity.getZip());

        dto.setEmergencyFirstName(entity.getEmergencyFirstName());
        dto.setEmergencyMiddleName(entity.getEmergencyMiddleName());
        dto.setEmergencyLastName(entity.getEmergencyLastName());
        dto.setEmergencySuffix(entity.getEmergencySuffix());
        dto.setEmergencyPhone(entity.getEmergencyPhone());
        dto.setEmergencyRelationship(entity.getEmergencyRelationship());

        dto.setCaregiverFirstName(entity.getCaregiverFirstName());
        dto.setCaregiverMiddleName(entity.getCaregiverMiddleName());
        dto.setCaregiverLastName(entity.getCaregiverLastName());
        dto.setCaregiverSuffix(entity.getCaregiverSuffix());
        dto.setCaregiverPhone(entity.getCaregiverPhone());
        dto.setCaregiverRelationship(entity.getCaregiverRelationship());
        dto.setCaregiverAddress1(entity.getCaregiverAddress1());
        dto.setCaregiverAddress2(entity.getCaregiverAddress2());
        dto.setCaregiverCity(entity.getCaregiverCity());
        dto.setCaregiverState(entity.getCaregiverState());
        dto.setCaregiverZip(entity.getCaregiverZip());
        dto.setCaregiverComment(entity.getCaregiverComment());

        dto.setEmploymentStatus(entity.getEmploymentStatus());
        dto.setEmployerName(entity.getEmployerName());
        dto.setEmployerPhone(entity.getEmployerPhone());
        dto.setEmployerAddress1(entity.getEmployerAddress1());
        dto.setEmployerAddress2(entity.getEmployerAddress2());
        dto.setEmployerCity(entity.getEmployerCity());
        dto.setEmployerState(entity.getEmployerState());
        dto.setEmployerZip(entity.getEmployerZip());

        dto.setChartNo(entity.getChartNo());
        dto.setDateRegistered(entity.getDateRegistered());
        dto.setAccountType(entity.getAccountType());
        dto.setDateOfFirstOccurrence(entity.getDateOfFirstOccurrence());
        dto.setAccountStatus(entity.getAccountStatus());
        dto.setAccountSecondaryStatus(entity.getAccountSecondaryStatus());

        dto.setReferralFirstName(entity.getReferralFirstName());
        dto.setReferralMiddleName(entity.getReferralMiddleName());
        dto.setReferralPhone(entity.getReferralPhone());
        dto.setReferralRelationship(entity.getReferralRelationship());
        dto.setReferralAddress1(entity.getReferralAddress1());
        dto.setReferralAddress2(entity.getReferralAddress2());
        dto.setReferralCity(entity.getReferralCity());
        dto.setReferralState(entity.getReferralState());
        dto.setReferralZip(entity.getReferralZip());
        dto.setReferralComment(entity.getReferralComment());

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
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        entity.setSuffix(dto.getSuffix());
        entity.setPreviousFirstName(dto.getPreviousFirstName());
        entity.setDob(dto.getDob());
        entity.setSex(dto.getSex());
        entity.setSexualOrientation(dto.getSexualOrientation());
        entity.setGenderIdentity(dto.getGenderIdentity());
        entity.setRace(dto.getRace());
        entity.setEthnicity(dto.getEthnicity());
        entity.setLanguage(dto.getLanguage());
        entity.setCountry(dto.getCountry());
        entity.setSsn(dto.getSsn());
        entity.setMaritalStatus(dto.getMaritalStatus());
        entity.setMultipleBirth(dto.getMultipleBirth());
        entity.setBirthOrder(dto.getBirthOrder());
        entity.setMothersMaidenName(dto.getMothersMaidenName());
        entity.setResidentOfPractice(dto.getResidentOfPractice());

        entity.setHomePhone(dto.getHomePhone());
        entity.setWorkPhone(dto.getWorkPhone());
        entity.setWorkPhoneExt(dto.getWorkPhoneExt());
        entity.setCellPhone(dto.getCellPhone());
        entity.setEmail(dto.getEmail());
        entity.setContactPreference(dto.getContactPreference());
        entity.setAddress1(dto.getAddress1());
        entity.setAddress2(dto.getAddress2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setZip(dto.getZip());

        entity.setEmergencyFirstName(dto.getEmergencyFirstName());
        entity.setEmergencyMiddleName(dto.getEmergencyMiddleName());
        entity.setEmergencyLastName(dto.getEmergencyLastName());
        entity.setEmergencySuffix(dto.getEmergencySuffix());
        entity.setEmergencyPhone(dto.getEmergencyPhone());
        entity.setEmergencyRelationship(dto.getEmergencyRelationship());

        entity.setCaregiverFirstName(dto.getCaregiverFirstName());
        entity.setCaregiverMiddleName(dto.getCaregiverMiddleName());
        entity.setCaregiverLastName(dto.getCaregiverLastName());
        entity.setCaregiverSuffix(dto.getCaregiverSuffix());
        entity.setCaregiverPhone(dto.getCaregiverPhone());
        entity.setCaregiverRelationship(dto.getCaregiverRelationship());
        entity.setCaregiverAddress1(dto.getCaregiverAddress1());
        entity.setCaregiverAddress2(dto.getCaregiverAddress2());
        entity.setCaregiverCity(dto.getCaregiverCity());
        entity.setCaregiverState(dto.getCaregiverState());
        entity.setCaregiverZip(dto.getCaregiverZip());
        entity.setCaregiverComment(dto.getCaregiverComment());

        entity.setEmploymentStatus(dto.getEmploymentStatus());
        entity.setEmployerName(dto.getEmployerName());
        entity.setEmployerPhone(dto.getEmployerPhone());
        entity.setEmployerAddress1(dto.getEmployerAddress1());
        entity.setEmployerAddress2(dto.getEmployerAddress2());
        entity.setEmployerCity(dto.getEmployerCity());
        entity.setEmployerState(dto.getEmployerState());
        entity.setEmployerZip(dto.getEmployerZip());

        entity.setChartNo(dto.getChartNo());
        entity.setDateRegistered(dto.getDateRegistered());
        entity.setAccountType(dto.getAccountType());
        entity.setDateOfFirstOccurrence(dto.getDateOfFirstOccurrence());
        entity.setAccountStatus(dto.getAccountStatus());
        entity.setAccountSecondaryStatus(dto.getAccountSecondaryStatus());

        entity.setReferralFirstName(dto.getReferralFirstName());
        entity.setReferralMiddleName(dto.getReferralMiddleName());
        entity.setReferralPhone(dto.getReferralPhone());
        entity.setReferralRelationship(dto.getReferralRelationship());
        entity.setReferralAddress1(dto.getReferralAddress1());
        entity.setReferralAddress2(dto.getReferralAddress2());
        entity.setReferralCity(dto.getReferralCity());
        entity.setReferralState(dto.getReferralState());
        entity.setReferralZip(dto.getReferralZip());
        entity.setReferralComment(dto.getReferralComment());

        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }
}
