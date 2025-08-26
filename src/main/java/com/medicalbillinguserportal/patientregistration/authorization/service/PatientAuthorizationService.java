package com.medicalbillinguserportal.patientregistration.authorization.service;

import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.mapper.PatientAuthorizationConverter;
import com.medicalbillinguserportal.patientregistration.authorization.repo.PatientAuthorizationRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientAuthorizationService {

    @Autowired
    public PatientAuthorizationRepo patientAuthorizationRepo;

    @Autowired
    public PatientInfoRepository patientInfoRepository;

    public PatientAuthorizationDto savePatientAuthorization(PatientAuthorizationDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));
        PatientAuthorizationEntity patientAuthorizationEntity = PatientAuthorizationConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientAuthorizationDto patientAuthorizationDto= PatientAuthorizationConverter.toDTO(patientAuthorizationRepo.save(patientAuthorizationEntity));
        return patientAuthorizationDto;
    }
}
