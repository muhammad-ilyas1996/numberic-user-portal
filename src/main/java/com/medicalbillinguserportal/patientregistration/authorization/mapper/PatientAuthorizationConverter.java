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
        entity.setServicePerformer(dto.getServicePerformer());
        entity.setAuthorizationNumber(dto.getAuthorizationNumber());
        entity.setAuthorizationDate(dto.getAuthorizationDate());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setRequestedDate(dto.getRequestedDate());
        entity.setReferredBy(dto.getReferredBy());
        entity.setPayer(dto.getPayer());
        entity.setContactPerson(dto.getContactPerson());
        entity.setPhoneNo(dto.getPhoneNo());
        entity.setAuthorizationLimitation(dto.getAuthorizationLimitation());
        entity.setTimeRestriction(dto.getTimeRestriction());
        entity.setTimeRestrictionPer(dto.getTimeRestrictionPer());
        entity.setPlaceOfService(dto.getPlaceOfService());
        entity.setStatus(dto.getStatus());
        entity.setNotes(dto.getNotes());

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
        dto.setServicePerformer(entity.getServicePerformer());
        dto.setAuthorizationNumber(entity.getAuthorizationNumber());
        dto.setAuthorizationDate(entity.getAuthorizationDate());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setRequestedDate(entity.getRequestedDate());
        dto.setReferredBy(entity.getReferredBy());
        dto.setPayer(entity.getPayer());
        dto.setContactPerson(entity.getContactPerson());
        dto.setPhoneNo(entity.getPhoneNo());
        dto.setAuthorizationLimitation(entity.getAuthorizationLimitation());
        dto.setTimeRestriction(entity.getTimeRestriction());
        dto.setTimeRestrictionPer(entity.getTimeRestrictionPer());
        dto.setPlaceOfService(entity.getPlaceOfService());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());

        if (entity.getPatientInfoEntity() != null) {
            dto.setPatientId(entity.getPatientInfoEntity().getId());
        }
        return dto;
    }
}
