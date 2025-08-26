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
        dto.setReferringProvider(entity.getReferringProvider());
        dto.setPriorAuthorization(entity.getPriorAuthorization());
        dto.setOtherReferralSource(entity.getOtherReferralSource());

        dto.setDefaultPCP(entity.getDefaultPCP());
        dto.setLastSeenByPCP(entity.getLastSeenByPCP());

        dto.setDefaultPharmacy(entity.getDefaultPharmacy());
        dto.setPharmacyPhoneNumber(entity.getPharmacyPhoneNumber());

        return dto;
    }

    public static PatientAssociationEntity toEntity(PatientAssociationDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientAssociationEntity entity = new PatientAssociationEntity();

        entity.setId(dto.getId());
        entity.setReferringProvider(dto.getReferringProvider());
        entity.setPriorAuthorization(dto.getPriorAuthorization());
        entity.setOtherReferralSource(dto.getOtherReferralSource());

        entity.setDefaultPCP(dto.getDefaultPCP());
        entity.setLastSeenByPCP(dto.getLastSeenByPCP());

        entity.setDefaultPharmacy(dto.getDefaultPharmacy());
        entity.setPharmacyPhoneNumber(dto.getPharmacyPhoneNumber());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
