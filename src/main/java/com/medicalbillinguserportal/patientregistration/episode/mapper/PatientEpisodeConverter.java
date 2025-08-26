package com.medicalbillinguserportal.patientregistration.episode.mapper;

import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientEpisodeConverter {
    public static PatientEpisodeDto toDTO(PatientEpisodeEntity entity) {
        if (entity == null) return null;

        PatientEpisodeDto dto = new PatientEpisodeDto();
        dto.setId(entity.getId());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDiagnosis(entity.getDiagnosis());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());

        return dto;
    }

    public static PatientEpisodeEntity toEntity(PatientEpisodeDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientEpisodeEntity entity = new PatientEpisodeEntity();
        entity.setId(dto.getId());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDiagnosis(dto.getDiagnosis());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
