package com.medicalbillinguserportal.patientregistration.guarantorinfo.service;

import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.episode.mapper.PatientEpisodeConverter;
import com.medicalbillinguserportal.patientregistration.episode.repo.PatientEpisodeRepo;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.dto.PatientGuarantorDto;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.entity.PatientGuarantorEntity;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.mapper.PatientGuarantorConverter;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.repository.PatientGuarantorRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientGuarantorService {

    @Autowired
    public PatientInfoRepository patientInfoRepository;

    @Autowired
    public PatientGuarantorRepo patientGuarantorRepo;

    public PatientGuarantorDto savePatientGuarantor(PatientGuarantorDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));
        PatientGuarantorEntity patientEpisodeEntity = PatientGuarantorConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientGuarantorDto patientGuarantorDto= PatientGuarantorConverter.toDto(patientGuarantorRepo.save(patientEpisodeEntity));
        return patientGuarantorDto;
    }
}
