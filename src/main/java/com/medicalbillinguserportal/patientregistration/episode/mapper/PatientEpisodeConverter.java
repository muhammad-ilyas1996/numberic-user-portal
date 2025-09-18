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
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDefaultEpisode(entity.getDefaultEpisode());
        dto.setEmployement(entity.getEmployement());
        dto.setAutoAccident(entity.getAutoAccident());
        dto.setState(entity.getState());
        dto.setOtherAccident(entity.getOtherAccident());

        dto.setDateOfCurrentIllness(entity.getDateOfCurrentIllness());
        dto.setDateDisAbilityBegin(entity.getDateDisAbilityBegin());
        dto.setDateDisAbilityEnd(entity.getDateDisAbilityEnd());
        dto.setDateHospitalAdmission(entity.getDateHospitalAdmission());
        dto.setDateHospitalDischanrge(entity.getDateHospitalDischanrge());
        dto.setDateAssumedCare(entity.getDateAssumedCare());
        dto.setDateRelinquishedCare(entity.getDateRelinquishedCare());

        dto.setAccidentType(entity.getAccidentType());
        dto.setAccidentDate(entity.getAccidentDate());
        dto.setAdditionalInfoCombo(entity.getAdditionalInfoCombo());
        dto.setAdditionalInfo(entity.getAdditionalInfo());

        dto.setSpecialProgramCode(entity.getSpecialProgramCode());
        dto.setClaimDelayReason(entity.getClaimDelayReason());
        dto.setClaimNotes(entity.getClaimNotes());
        dto.setHomeBound(entity.getHomeBound());


        return dto;
    }

    public static PatientEpisodeEntity toEntity(PatientEpisodeDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientEpisodeEntity entity = new PatientEpisodeEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDefaultEpisode(dto.getDefaultEpisode());
        entity.setEmployement(dto.getEmployement());
        entity.setAutoAccident(dto.getAutoAccident());
        entity.setState(dto.getState());
        entity.setOtherAccident(dto.getOtherAccident());

        entity.setDateOfCurrentIllness(dto.getDateOfCurrentIllness());
        entity.setDateDisAbilityBegin(dto.getDateDisAbilityBegin());
        entity.setDateDisAbilityEnd(dto.getDateDisAbilityEnd());
        entity.setDateHospitalAdmission(dto.getDateHospitalAdmission());
        entity.setDateHospitalDischanrge(dto.getDateHospitalDischanrge());
        entity.setDateAssumedCare(dto.getDateAssumedCare());
        entity.setDateRelinquishedCare(dto.getDateRelinquishedCare());

        entity.setAccidentType(dto.getAccidentType());
        entity.setAccidentDate(dto.getAccidentDate());

        entity.setAdditionalInfoCombo(dto.getAdditionalInfoCombo());
        entity.setAdditionalInfo(dto.getAdditionalInfo());

        entity.setSpecialProgramCode(dto.getSpecialProgramCode());
        entity.setClaimDelayReason(dto.getClaimDelayReason());
        entity.setClaimNotes(dto.getClaimNotes());
        entity.setHomeBound(dto.getHomeBound());

        entity.setPatientInfoEntity(patientInfoEntity);
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
