package com.medicalbillinguserportal.patientregistration.patientinformation.mapper;

import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoIdDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;

public class PatientInfoIdConverter {

    public static PatientInfoIdDto toDto(PatientInfoEntity entity)
    {
        PatientInfoIdDto dto=new PatientInfoIdDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setAddress1(entity.getAddress1());
        dto.setCellPhone(entity.getCellPhone());
        dto.setEmail(entity.getEmail());
        return dto;
    }
    public static PatientInfoEntity toEntity(PatientInfoIdDto dto)
    {
        PatientInfoEntity entity =new PatientInfoEntity();
        entity.setId(dto.getId());
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        entity.setAddress1(dto.getAddress1());
        entity.setCellPhone(dto.getCellPhone());
        entity.setEmail(dto.getEmail());
        return entity;
    }

}
