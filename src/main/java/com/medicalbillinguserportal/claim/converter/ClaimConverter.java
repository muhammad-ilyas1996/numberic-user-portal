package com.medicalbillinguserportal.claim.converter;

import com.medicalbillinguserportal.claim.dto.*;
import com.medicalbillinguserportal.claim.entity.*;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class ClaimConverter {

    public ClaimEntity toEntity(ClaimDTO dto, PatientInfoEntity patient, User currentUser) {
        ClaimEntity entity = new ClaimEntity();

        entity.setClaimId(dto.getClaimId());
        entity.setPatientInfoEntity(patient);

        entity.setClaimTransmission(dto.getClaimTransmission());
        entity.setPayerAddress(dto.getPayerAddress());
        entity.setDateOfService(dto.getDateOfService());
        entity.setRenderingProvider(dto.getRenderingProvider());
        entity.setLocation(dto.getLocation());
        entity.setSupervisingProvider(dto.getSupervisingProvider());
        entity.setBillingProvider(dto.getBillingProvider());
        entity.setPayToProvider(dto.getPayToProvider());
        entity.setReferringProvider(dto.getReferringProvider());
        entity.setServicingProvider(dto.getServicingProvider());
        entity.setFeeSchedule(dto.getFeeSchedule());
        entity.setPayToLocation(dto.getPayToLocation());

        entity.setSalesRep(dto.getSalesRep());
        entity.setClinicName(dto.getClinicName());
        entity.setAccession(dto.getAccession());
        entity.setAmbulanceInfo(dto.getAmbulanceInfo());
        entity.setEpStdReferral(dto.getEpStdReferral());
        entity.setPaperWork(dto.getPaperWork());
        entity.setSpecialManipulation(dto.getSpecialManipulation());
        entity.setHomeBound(dto.getHomeBound());

        entity.setPhCode1(dto.getPhCode1());
        entity.setPhCode2(dto.getPhCode2());
        entity.setPhCode3(dto.getPhCode3());
        entity.setPhCode4(dto.getPhCode4());
        entity.setPhCode5(dto.getPhCode5());
        entity.setPhCode6(dto.getPhCode6());
        entity.setPhCode7(dto.getPhCode7());
        entity.setPhCode8(dto.getPhCode8());
        entity.setPhCode9(dto.getPhCode9());
        entity.setPhCode10(dto.getPhCode10());
        entity.setPhCode11(dto.getPhCode11());
        entity.setPhCode12(dto.getPhCode12());

        entity.setTotal(dto.getTotal());
        entity.setInsuranceTotal(dto.getInsuranceTotal());

        //Service lines
        entity.setServiceLines(
                dto.getServiceLines().stream().map(lineDTO -> {
                    ClaimServiceLineEntity line = new ClaimServiceLineEntity();
                    line.setFromDate(lineDTO.getFromDate());
                    line.setToDate(lineDTO.getToDate());
                    line.setPos(lineDTO.getPos());
                    line.setEmg(lineDTO.getEmg());
                    line.setCptCode(lineDTO.getCptCode());
                    line.setModifiers(lineDTO.getModifiers());
                    line.setDiagnosisPointer(lineDTO.getDiagnosisPointer());
                    line.setFee(lineDTO.getFee());
                    line.setUnits(lineDTO.getUnits());
                    line.setCharge(lineDTO.getCharge());
                    line.setEpStd(lineDTO.getEpStd());
                    line.setFamilyPlan(lineDTO.getFamilyPlan());
                    line.setClaim(entity);
                    return line;
                }).collect(Collectors.toList())
        );

        entity.setAmountCollectedTotal(dto.getAmountCollectedTotal());

        //Payments
        entity.setClaimPayment(
                dto.getClaimPayment().stream().map(payDTO -> {
                    ClaimPayment pay = new ClaimPayment();
                    pay.setPaymentId(payDTO.getPaymentId());
                    pay.setPaymentDate(payDTO.getPaymentDate());
                    pay.setPaymentAmount(payDTO.getPaymentAmount());
                    pay.setCoPay(payDTO.getCoPay());
                    pay.setSelfPay(payDTO.getSelfPay());
                    pay.setOutstandingBalance(payDTO.getOutstandingBalance());
                    pay.setNonClaimCharge(payDTO.getNonClaimCharge());
                    pay.setUnappliedAmount(payDTO.getUnappliedAmount());
                    pay.setApplyToClaim(payDTO.getApplyToClaim());
                    pay.setClaim(entity);
                    return pay;
                }).collect(Collectors.toList())
        );

        entity.setDescription(dto.getDescription());
        entity.setEnteredBy(dto.getEnteredBy());
        entity.setEnteredDate(dto.getEnteredDate());
        entity.setEnteredNotes(dto.getEnteredNotes());
        entity.setLedgerDate(dto.getLedgerDate());

        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
    public ClaimFullResponseDto toFullDto(ClaimEntity entity, User currentUser) {
        ClaimFullResponseDto dto = new ClaimFullResponseDto();

        dto.setClaimId(entity.getClaimId());
        dto.setPatientId(entity.getPatientInfoEntity().getId());

        dto.setClaimTransmission(entity.getClaimTransmission());
        dto.setPayerAddress(entity.getPayerAddress());
        dto.setDateOfService(entity.getDateOfService());
        dto.setRenderingProvider(entity.getRenderingProvider());
        dto.setLocation(entity.getLocation());
        dto.setSupervisingProvider(entity.getSupervisingProvider());
        dto.setBillingProvider(entity.getBillingProvider());
        dto.setPayToProvider(entity.getPayToProvider());
        dto.setReferringProvider(entity.getReferringProvider());
        dto.setServicingProvider(entity.getServicingProvider());
        dto.setFeeSchedule(entity.getFeeSchedule());
        dto.setPayToLocation(entity.getPayToLocation());

        dto.setSalesRep(entity.getSalesRep());
        dto.setClinicName(entity.getClinicName());
        dto.setAccession(entity.getAccession());
        dto.setAmbulanceInfo(entity.getAmbulanceInfo());
        dto.setEpStdReferral(entity.getEpStdReferral());
        dto.setPaperWork(entity.getPaperWork());
        dto.setSpecialManipulation(entity.getSpecialManipulation());
        dto.setHomeBound(entity.getHomeBound());

        dto.setPhCode1(entity.getPhCode1());
        dto.setPhCode2(entity.getPhCode2());
        dto.setPhCode3(entity.getPhCode3());
        dto.setPhCode4(entity.getPhCode4());
        dto.setPhCode5(entity.getPhCode5());
        dto.setPhCode6(entity.getPhCode6());
        dto.setPhCode7(entity.getPhCode7());
        dto.setPhCode8(entity.getPhCode8());
        dto.setPhCode9(entity.getPhCode9());
        dto.setPhCode10(entity.getPhCode10());
        dto.setPhCode11(entity.getPhCode11());
        dto.setPhCode12(entity.getPhCode12());

        dto.setTotal(entity.getTotal());
        dto.setInsuranceTotal(entity.getInsuranceTotal());

        // service lines
        dto.setServiceLines(
                entity.getServiceLines().stream().map(line -> {
                    ClaimServiceLineDTO l = new ClaimServiceLineDTO();
                    l.setId(line.getId());
                    l.setFromDate(line.getFromDate());
                    l.setToDate(line.getToDate());
                    l.setPos(line.getPos());
                    l.setEmg(line.getEmg());
                    l.setCptCode(line.getCptCode());
                    l.setModifiers(line.getModifiers());
                    l.setDiagnosisPointer(line.getDiagnosisPointer());
                    l.setFee(line.getFee());
                    l.setUnits(line.getUnits());
                    l.setCharge(line.getCharge());
                    l.setEpStd(line.getEpStd());
                    l.setFamilyPlan(line.getFamilyPlan());
                    return l;
                }).toList()
        );

        // payments
        dto.setClaimPayment(
                entity.getClaimPayment().stream().map(p -> {
                    ClaimPaymentDTO pay = new ClaimPaymentDTO();
                    pay.setPaymentId(p.getPaymentId());
                    pay.setPaymentDate(p.getPaymentDate());
                    pay.setPaymentAmount(p.getPaymentAmount());
                    pay.setCoPay(p.getCoPay());
                    pay.setSelfPay(p.getSelfPay());
                    pay.setOutstandingBalance(p.getOutstandingBalance());
                    pay.setNonClaimCharge(p.getNonClaimCharge());
                    pay.setUnappliedAmount(p.getUnappliedAmount());
                    pay.setApplyToClaim(p.getApplyToClaim());
                    return pay;
                }).toList()
        );

        dto.setDescription(entity.getDescription());
        dto.setEnteredBy(entity.getEnteredBy());
        dto.setEnteredDate(entity.getEnteredDate());
        dto.setEnteredNotes(entity.getEnteredNotes());
        dto.setLedgerDate(entity.getLedgerDate());

        if (currentUser != null) {
            // Auditing fields
            dto.setCreatedBy(currentUser.getUserId().toString());
            dto.setModifiedBy(currentUser.getUserId().toString());
        }
        dto.setCreatedOn(new Date());
        dto.setModifiedOn(new Date());
        return dto;
    }

}
