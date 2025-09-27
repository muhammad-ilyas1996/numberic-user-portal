package com.medicalbillinguserportal.patientregistration.patientinformation.mapper;

import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoIdDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;

public class PatientInfoIdConverter {

    public static PatientInfoIdDto toDto(PatientInfoEntity entity)
    {
        PatientInfoIdDto dto=new PatientInfoIdDto();
        dto.setId(entity.getId());
        dto.setPatientFirstName(entity.getPatientFirstName());
        dto.setPatientMiddleName(entity.getPatientMiddleName());
        dto.setPatientLastName(entity.getPatientLastName());
        dto.setPatientAddress1(entity.getPatientAddress1());
        dto.setCellPhone(entity.getCellPhone());
        dto.setEmail(entity.getEmail());
        return dto;
    }
    public static PatientInfoEntity toEntity(PatientInfoIdDto dto)
    {
        PatientInfoEntity entity =new PatientInfoEntity();
        entity.setId(dto.getId());
        entity.setPatientFirstName(dto.getPatientFirstName());
        entity.setPatientMiddleName(dto.getPatientMiddleName());
        entity.setPatientLastName(dto.getPatientLastName());
        entity.setPatientAddress1(dto.getPatientAddress1());
        entity.setCellPhone(dto.getCellPhone());
        entity.setEmail(dto.getEmail());
        return entity;
    }

}
