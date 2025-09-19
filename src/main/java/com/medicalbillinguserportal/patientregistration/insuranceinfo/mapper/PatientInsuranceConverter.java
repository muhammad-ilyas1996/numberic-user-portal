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
        dto.setInsuranceLevel(entity.getInsuranceLevel());
        dto.setInsuranceStatus(entity.getInsuranceStatus());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setPayerName(entity.getPayerName());
        dto.setPayerAddress(entity.getPayerAddress());
        dto.setPayerClass(entity.getPayerClass());
        dto.setPayerType(entity.getPayerType());
        dto.setPlanName(entity.getPlanName());
        dto.setGroupNo(entity.getGroupNo());
        dto.setInsuranceTypeCode(entity.getInsuranceTypeCode());
        dto.setCoPay(entity.getCoPay());
        dto.setCoIns(entity.getCoIns());
        dto.setAcceptAssignment(entity.getAcceptAssignment());
        dto.setPatientRelationship(entity.getPatientRelationship());
        dto.setInsuredFirstName(entity.getInsuredFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setInsuredLastName(entity.getInsuredLastName());
        dto.setInsuredDob(entity.getInsuredDob());
        dto.setInsuredSex(entity.getInsuredSex());
        dto.setCountry(entity.getCountry());
        dto.setAddress1(entity.getAddress1());
        dto.setPlanName1(entity.getPlanName1());
        dto.setGroupNo1(entity.getGroupNo1());
        dto.setInsuranceTypeCode1(entity.getInsuranceTypeCode1());
        dto.setCoPay1(entity.getCoPay1());
        dto.setCoIns1(entity.getCoIns1());
        dto.setAcceptAssignment1(entity.getAcceptAssignment1());
        dto.setAddress2(entity.getAddress2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setZip(entity.getZip());
        dto.setHomePhone(entity.getHomePhone());


        dto.setPatientId(entity.getPatientInfoEntity().getId());
        return dto;
    }

    public static PatientInsuranceEntity toEntity(PatientInsuranceDto dto, PatientInfoEntity patientInfoEntity, User currentUser) {
        if (dto == null) return null;

        PatientInsuranceEntity entity = new PatientInsuranceEntity();

        entity.setId(dto.getId());
        entity.setInsuranceLevel(dto.getInsuranceLevel());
        entity.setInsuranceStatus(dto.getInsuranceStatus());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setPayerName(dto.getPayerName());
        entity.setPayerAddress(dto.getPayerAddress());
        entity.setPayerClass(dto.getPayerClass());
        entity.setPayerType(dto.getPayerType());
        entity.setPlanName(dto.getPlanName());
        entity.setGroupNo(dto.getGroupNo());
        entity.setInsuranceTypeCode(dto.getInsuranceTypeCode());
        entity.setCoPay(dto.getCoPay());
        entity.setCoIns(dto.getCoIns());
        entity.setAcceptAssignment(dto.getAcceptAssignment());
        entity.setPatientRelationship(dto.getPatientRelationship());
        entity.setInsuredFirstName(dto.getInsuredFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setInsuredLastName(dto.getInsuredLastName());
        entity.setInsuredDob(dto.getInsuredDob());
        entity.setInsuredSex(dto.getInsuredSex());
        entity.setCountry(dto.getCountry());
        entity.setAddress1(dto.getAddress1());
        entity.setPlanName1(dto.getPlanName1());
        entity.setGroupNo1(dto.getGroupNo1());
        entity.setInsuranceTypeCode1(dto.getInsuranceTypeCode1());
        entity.setCoPay1(dto.getCoPay1());
        entity.setCoIns1(dto.getCoIns1());
        entity.setAcceptAssignment1(dto.getAcceptAssignment1());
        entity.setAddress2(dto.getAddress2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setZip(dto.getZip());
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
