package com.medicalbillinguserportal.patientregistration.association.mapper;

import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientOtherAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientOtherAssociationEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

        // patientId mapping
        dto.setPatientId(entity.getPatientInfoEntity() != null ? entity.getPatientInfoEntity().getId() : null);

        // map child list
        if (entity.getPatientOtherAssociation() != null) {
            List<PatientOtherAssociationDto> childDtos = entity.getPatientOtherAssociation()
                    .stream()
                    .map(PatientOtherAssociationConverter::toDTO)
                    .collect(Collectors.toList());
            dto.setPatientOtherAssociationDto(childDtos);
        }

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

        entity.setPatientInfoEntity(patientInfoEntity);

        // map child list
        if (dto.getPatientOtherAssociationDto() != null) {
            List<PatientOtherAssociationEntity> children = dto.getPatientOtherAssociationDto()
                    .stream()
                    .map(childDto -> {
                        PatientOtherAssociationEntity childEntity = PatientOtherAssociationConverter.toEntity(childDto, currentUser);
                        childEntity.setPatientAssociation(entity); // set parent reference
                        return childEntity;
                    })
                    .collect(Collectors.toList());
            entity.setPatientOtherAssociation(children);
        }

        // audit fields
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }
}
