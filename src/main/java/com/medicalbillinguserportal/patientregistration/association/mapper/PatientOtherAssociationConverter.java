package com.medicalbillinguserportal.patientregistration.association.mapper;

import com.medicalbillinguserportal.patientregistration.association.dto.PatientOtherAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientOtherAssociationEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientOtherAssociationConverter {
    public static PatientOtherAssociationDto toDTO(PatientOtherAssociationEntity entity) {
        if (entity == null) return null;

        PatientOtherAssociationDto dto = new PatientOtherAssociationDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setRole(entity.getRole());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setExt(entity.getExt());
        dto.setFax(entity.getFax());
        return dto;
    }

    public static PatientOtherAssociationEntity toEntity(PatientOtherAssociationDto dto, User currentUser) {
        if (dto == null) return null;

        PatientOtherAssociationEntity entity = new PatientOtherAssociationEntity();
        entity.setId(dto.getId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setRole(dto.getRole());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setExt(dto.getExt());
        entity.setFax(dto.getFax());

        // audit fields
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
