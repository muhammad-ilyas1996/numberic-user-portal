package com.medicalbillinguserportal.patientregistration.message.mapper;

import com.medicalbillinguserportal.patientregistration.message.dto.PatientMessageDto;
import com.medicalbillinguserportal.patientregistration.message.entity.PatientMessageEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.time.LocalDateTime;
import java.util.Date;

public class PatientMessageConverter {
    public static PatientMessageDto toDTO(PatientMessageEntity entity) {
        if (entity == null) return null;

        PatientMessageDto dto = new PatientMessageDto();
        dto.setId(entity.getId());
        dto.setNote(entity.getNote());
        dto.setType(entity.getType());
        dto.setAlertOnScheduling(entity.getAlertOnScheduling());
        dto.setAlertOnBilling(entity.getAlertOnBilling());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    public static PatientMessageEntity toEntity(PatientMessageDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientMessageEntity entity = new PatientMessageEntity();
        entity.setId(dto.getId());
        entity.setNote(dto.getNote());
        entity.setType(dto.getType());
        entity.setAlertOnScheduling(Boolean.TRUE.equals(dto.getAlertOnScheduling()));
        entity.setAlertOnBilling(Boolean.TRUE.equals(dto.getAlertOnBilling()));
        entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
