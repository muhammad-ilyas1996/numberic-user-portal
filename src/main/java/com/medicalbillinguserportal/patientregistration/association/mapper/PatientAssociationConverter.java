package com.medicalbillinguserportal.patientregistration.association.mapper;

import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientAssociationConverter {
    public static PatientAssociationDto toDTO(PatientAssociationEntity entity) {
        if (entity == null) return null;

        PatientAssociationDto dto = new PatientAssociationDto();

        dto.setId(entity.getId());
        dto.setAssociationProvider(entity.getAssociationProvider());
        dto.setAssociationReferringProvider(entity.getAssociationReferringProvider());
        dto.setAssociationPriorAuthorization(entity.getAssociationPriorAuthorization());
        dto.setAssociationOtherReferralSource(entity.getAssociationOtherReferralSource());
        dto.setPatientOutsidePCP(entity.getPatientOutsidePCP());
        dto.setLastSeenByPCP(entity.getLastSeenByPCP());
        dto.setAssociationFirstName(entity.getAssociationFirstName());
        dto.setAssociationLastName(entity.getAssociationLastName());
        dto.setAssociationRole(entity.getAssociationRole());
        dto.setAssociationEmail(entity.getAssociationEmail());
        dto.setAssociationPhone(entity.getAssociationPhone());
        dto.setAssociationExt(entity.getAssociationExt());
        dto.setAssociationFax(entity.getAssociationFax());

        return dto;
    }

    public static PatientAssociationEntity toEntity(PatientAssociationDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientAssociationEntity entity = new PatientAssociationEntity();

        entity.setId(dto.getId());
        entity.setAssociationProvider(dto.getAssociationProvider());
        entity.setAssociationReferringProvider(dto.getAssociationReferringProvider());
        entity.setAssociationPriorAuthorization(dto.getAssociationPriorAuthorization());
        entity.setAssociationOtherReferralSource(dto.getAssociationOtherReferralSource());
        entity.setPatientOutsidePCP(dto.getPatientOutsidePCP());
        entity.setLastSeenByPCP(dto.getLastSeenByPCP());
        entity.setAssociationFirstName(dto.getAssociationFirstName());
        entity.setAssociationLastName(dto.getAssociationLastName());
        entity.setAssociationRole(dto.getAssociationRole());
        entity.setAssociationEmail(dto.getAssociationEmail());
        entity.setAssociationPhone(dto.getAssociationPhone());
        entity.setAssociationExt(dto.getAssociationExt());
        entity.setAssociationFax(dto.getAssociationFax());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
