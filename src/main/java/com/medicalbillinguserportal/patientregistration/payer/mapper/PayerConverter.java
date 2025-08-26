package com.medicalbillinguserportal.patientregistration.payer.mapper;

import com.medicalbillinguserportal.common.constant.PayerStatus;
import com.medicalbillinguserportal.patientregistration.payer.dto.PayerDTO;
import com.medicalbillinguserportal.patientregistration.payer.entity.PayerEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class PayerConverter {
    public static PayerEntity toEntity(PayerDTO dto, User currentUser) {
        PayerEntity entity = new PayerEntity();
        entity.setPayer(dto.getPayer());
        entity.setAddress1(dto.getAddress1());
        entity.setAddress2(dto.getAddress2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setZip(dto.getZip());
        entity.setCountryCode(dto.getCountryCode());
        entity.setDefaultLab(dto.getDefaultLab());
        entity.setPhone(dto.getPhone());
        entity.setFax(dto.getFax());
        entity.setPayerType(dto.getPayerType());
        entity.setPayerClass(dto.getPayerClass());
        entity.setClaimFormat(dto.getClaimFormat());
        entity.setStatus(PayerStatus.valueOf(dto.getStatus()));
        //Audit
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }

    public static PayerDTO toDTO(PayerEntity entity) {
        PayerDTO dto = new PayerDTO();
        dto.setPayer(entity.getPayer());
        dto.setAddress1(entity.getAddress1());
        dto.setAddress2(entity.getAddress2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setZip(entity.getZip());
        dto.setCountryCode(entity.getCountryCode());
        dto.setDefaultLab(entity.getDefaultLab());
        dto.setPhone(entity.getPhone());
        dto.setFax(entity.getFax());
        dto.setPayerType(entity.getPayerType());
        dto.setPayerClass(entity.getPayerClass());
        dto.setClaimFormat(entity.getClaimFormat());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}
