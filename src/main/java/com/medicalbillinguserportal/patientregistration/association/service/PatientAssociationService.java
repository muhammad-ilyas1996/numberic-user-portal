package com.medicalbillinguserportal.patientregistration.association.service;

import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.association.mapper.PatientAssociationConverter;
import com.medicalbillinguserportal.patientregistration.association.repository.PatientAssociationRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientAssociationService {
    @Autowired
    public PatientAssociationRepo patientAssociationRepo;
    @Autowired
    public PatientInfoRepository patientInfoRepository;
    public PatientAssociationDto savePatientAssociation(PatientAssociationDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));

        PatientAssociationEntity patientAssociationEntity = PatientAssociationConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientAssociationDto patientAssociationDto=PatientAssociationConverter.toDTO(patientAssociationRepo.save(patientAssociationEntity));
        return patientAssociationDto;
    }
}
