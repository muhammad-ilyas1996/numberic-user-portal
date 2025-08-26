package com.medicalbillinguserportal.patientregistration.message.service;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.mapper.PatientInsuranceConverter;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.repository.PatientInsuranceRepo;
import com.medicalbillinguserportal.patientregistration.message.dto.PatientMessageDto;
import com.medicalbillinguserportal.patientregistration.message.entity.PatientMessageEntity;
import com.medicalbillinguserportal.patientregistration.message.mapper.PatientMessageConverter;
import com.medicalbillinguserportal.patientregistration.message.repo.PatientMessageRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientMessageService {

    @Autowired
    public PatientMessageRepo patientMessageRepo;
    @Autowired
    public PatientInfoRepository patientInfoRepository;

    public PatientMessageDto savePatientMessage(PatientMessageDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity =patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Not Found"));

        PatientMessageEntity patientMessageEntity= PatientMessageConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientMessageDto patientMessageDto=PatientMessageConverter.toDTO(patientMessageRepo.save(patientMessageEntity));
        return patientMessageDto;

    }
}
