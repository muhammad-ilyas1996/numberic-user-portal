package com.medicalbillinguserportal.patientregistration.guarantorinfo.mapper;

import com.medicalbillinguserportal.patientregistration.guarantorinfo.dto.PatientGuarantorDto;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.entity.PatientGuarantorEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientGuarantorConverter {
    public static PatientGuarantorDto toDto(PatientGuarantorEntity entity)
    {
        if (entity == null) return null;

        PatientGuarantorDto dto = new PatientGuarantorDto();

        dto.setId(entity.getId());
        dto.setRelationToPatient(entity.getRelationToPatient());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastNameOrOrg(entity.getLastNameOrOrg());
        dto.setDob(entity.getDob());
        dto.setSex(entity.getSex());

        dto.setRace(entity.getRace());
        dto.setEthnicity(entity.getEthnicity());
        dto.setLanguage(entity.getLanguage());
        dto.setCountry(entity.getCountry());

        dto.setSsn(entity.getSsn());

        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setMaritalStatus(entity.getMaritalStatus());
        dto.setEmploymentStatus(entity.getEmploymentStatus());

        dto.setHomePhone(entity.getHomePhone());
        dto.setWorkPhone(entity.getWorkPhone());
        dto.setWorkPhoneExt(entity.getWorkPhoneExt());
        dto.setCellPhone(entity.getCellPhone());
        dto.setEmail(entity.getEmail());

        dto.setPreviousAddress1(entity.getPreviousAddress1());
        dto.setPreviousAddress2(entity.getPreviousAddress2());
        dto.setPreviousCity(entity.getPreviousCity());
        dto.setPreviousState(entity.getPreviousState());
        dto.setPreviousZip(entity.getPreviousZip());

        return dto;
    }
    public static PatientGuarantorEntity toEntity(PatientGuarantorDto dto, PatientInfoEntity patientInfoEntity, User currentUser)
    {
        if (dto == null) return null;

        PatientGuarantorEntity entity = new PatientGuarantorEntity();

        entity.setId(dto.getId());
        entity.setRelationToPatient(dto.getRelationToPatient());
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastNameOrOrg(dto.getLastNameOrOrg());
        entity.setDob(dto.getDob());
        entity.setSex(dto.getSex());

        entity.setRace(dto.getRace());
        entity.setEthnicity(dto.getEthnicity());
        entity.setLanguage(dto.getLanguage());
        entity.setCountry(dto.getCountry());

        entity.setSsn(dto.getSsn());

        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setMaritalStatus(dto.getMaritalStatus());
        entity.setEmploymentStatus(dto.getEmploymentStatus());

        entity.setHomePhone(dto.getHomePhone());
        entity.setWorkPhone(dto.getWorkPhone());
        entity.setWorkPhoneExt(dto.getWorkPhoneExt());
        entity.setCellPhone(dto.getCellPhone());
        entity.setEmail(dto.getEmail());

        entity.setPreviousAddress1(dto.getPreviousAddress1());
        entity.setPreviousAddress2(dto.getPreviousAddress2());
        entity.setPreviousCity(dto.getPreviousCity());
        entity.setPreviousState(dto.getPreviousState());
        entity.setPreviousZip(dto.getPreviousZip());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }
}
