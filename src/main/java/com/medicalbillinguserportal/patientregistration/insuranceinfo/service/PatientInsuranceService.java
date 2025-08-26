package com.medicalbillinguserportal.patientregistration.insuranceinfo.service;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.mapper.PatientInsuranceConverter;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.repository.PatientInsuranceRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class PatientInsuranceService {

    @Autowired
    public PatientInsuranceRepo patientInsuranceRepo;
    @Autowired
    public PatientInfoRepository patientInfoRepository;

    public PatientInsuranceDto savePatientInsurance(PatientInsuranceDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity =patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Not Found"));

        PatientInsuranceEntity patientInsuranceEntity= PatientInsuranceConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientInsuranceDto patientInsuranceDto=PatientInsuranceConverter.toDto(patientInsuranceRepo.save(patientInsuranceEntity));
        return patientInsuranceDto;

    }
}
