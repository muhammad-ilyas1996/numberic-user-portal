package com.medicalbillinguserportal.patientregistration.insuranceinfo.mapper;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PatientInsuranceConverter {

    public static PatientInsuranceDto toDto(PatientInsuranceEntity entity)
    {
        PatientInsuranceDto dto = new PatientInsuranceDto();

        dto.setId(entity.getId());
        dto.setPlanName(entity.getPlanName());
        dto.setPolicyHolder(entity.getPolicyHolder());
        dto.setPolicyNumber(entity.getPolicyNumber());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setStatus(entity.getStatus());
        dto.setPayerName(entity.getPayerName());

        dto.setInsuranceStatus(entity.getInsuranceStatus());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setCurrentExpirationDate(entity.getCurrentExpirationDate());
        dto.setCurrentPayerName(entity.getCurrentPayerName());
        dto.setPayerAddress(entity.getPayerAddress());
        dto.setPayerClaim(entity.getPayerClaim());
        dto.setPayerType(entity.getPayerType());
        dto.setGroupNumber(entity.getGroupNumber());
        dto.setTypeCode(entity.getTypeCode());

        dto.setCoPay(entity.getCoPay());
        dto.setCoIns(entity.getCoIns());
        dto.setAcceptAssignment(entity.getAcceptAssignment());

        dto.setPatientRelationship(entity.getPatientRelationship());
        dto.setInsuredFirstName(entity.getInsuredFirstName());
        dto.setInsuredMiddleName(entity.getInsuredMiddleName());
        dto.setInsuredLastName(entity.getInsuredLastName());
        dto.setInsuredDOB(entity.getInsuredDOB());
        dto.setInsuredSex(entity.getInsuredSex());

        dto.setCountry(entity.getCountry());
        dto.setAddress1(entity.getAddress1());
        dto.setAddress2(entity.getAddress2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setHomePhone(entity.getHomePhone());

        dto.setPatientId(entity.getPatientInfoEntity().getId());
        return dto;
    }

    public static PatientInsuranceEntity toEntity(PatientInsuranceDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientInsuranceEntity entity = new PatientInsuranceEntity();

        entity.setId(dto.getId());
        entity.setPlanName(dto.getPlanName());
        entity.setPolicyHolder(dto.getPolicyHolder());
        entity.setPolicyNumber(dto.getPolicyNumber());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setStatus(dto.getStatus());
        entity.setPayerName(dto.getPayerName());

        entity.setInsuranceStatus(dto.getInsuranceStatus());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setCurrentExpirationDate(dto.getCurrentExpirationDate());
        entity.setCurrentPayerName(dto.getCurrentPayerName());
        entity.setPayerAddress(dto.getPayerAddress());
        entity.setPayerClaim(dto.getPayerClaim());
        entity.setPayerType(dto.getPayerType());
        entity.setGroupNumber(dto.getGroupNumber());
        entity.setTypeCode(dto.getTypeCode());

        entity.setCoPay(dto.getCoPay());
        entity.setCoIns(dto.getCoIns());
        entity.setAcceptAssignment(dto.getAcceptAssignment());

        entity.setPatientRelationship(dto.getPatientRelationship());
        entity.setInsuredFirstName(dto.getInsuredFirstName());
        entity.setInsuredMiddleName(dto.getInsuredMiddleName());
        entity.setInsuredLastName(dto.getInsuredLastName());
        entity.setInsuredDOB(dto.getInsuredDOB());
        entity.setInsuredSex(dto.getInsuredSex());

        entity.setCountry(dto.getCountry());
        entity.setAddress1(dto.getAddress1());
        entity.setAddress2(dto.getAddress2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setHomePhone(dto.getHomePhone());

        entity.setPatientInfoEntity(patientInfoEntity);

        //Audit Purpose
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }

}
