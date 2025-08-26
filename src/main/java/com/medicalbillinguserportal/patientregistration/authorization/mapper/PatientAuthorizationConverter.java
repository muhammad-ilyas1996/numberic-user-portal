package com.medicalbillinguserportal.patientregistration.authorization.mapper;

import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientAuthorizationConverter {
    public static PatientAuthorizationEntity toEntity(PatientAuthorizationDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientAuthorizationEntity entity = new PatientAuthorizationEntity();
        entity.setId(dto.getId());
        entity.setAuthorizationNumber(dto.getAuthorizationNumber());
        entity.setStatus(dto.getStatus());
        entity.setRequestedDate(dto.getRequestedDate());
        entity.setCurrentPayer(dto.getCurrentPayer());
        entity.setAuthorizedQty(dto.getAuthorizedQty());
        entity.setAuthAmountSum(dto.getAuthAmountSum());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setApprovalType(dto.getApprovalType());
        entity.setTest(dto.getTest());
        entity.setDate(dto.getDate());
        entity.setClaimNumber(dto.getClaimNumber());
        entity.setChapter(dto.getChapter());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;

    }

    public static PatientAuthorizationDto toDTO(PatientAuthorizationEntity entity) {
        if (entity == null) return null;

        PatientAuthorizationDto dto = new PatientAuthorizationDto();
        dto.setId(entity.getId());
        dto.setAuthorizationNumber(entity.getAuthorizationNumber());
        dto.setStatus(entity.getStatus());
        dto.setRequestedDate(entity.getRequestedDate());
        dto.setCurrentPayer(entity.getCurrentPayer());
        dto.setAuthorizedQty(entity.getAuthorizedQty());
        dto.setAuthAmountSum(entity.getAuthAmountSum());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setApprovalType(entity.getApprovalType());
        dto.setTest(entity.getTest());
        dto.setDate(entity.getDate());
        dto.setClaimNumber(entity.getClaimNumber());
        dto.setChapter(entity.getChapter());
        return dto;
    }
}
