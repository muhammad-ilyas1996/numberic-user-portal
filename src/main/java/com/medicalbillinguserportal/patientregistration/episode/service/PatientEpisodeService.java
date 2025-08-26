package com.medicalbillinguserportal.patientregistration.episode.service;

import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.mapper.PatientAuthorizationConverter;
import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.episode.mapper.PatientEpisodeConverter;
import com.medicalbillinguserportal.patientregistration.episode.repo.PatientEpisodeRepo;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientEpisodeService {
    @Autowired
    public PatientInfoRepository patientInfoRepository;

    @Autowired
    public PatientEpisodeRepo patientEpisodeRepo;

    public PatientEpisodeDto savePatientEpisode(PatientEpisodeDto dto, User currentUser)
    {
        PatientInfoEntity patientInfoEntity=patientInfoRepository.findById(dto.getPatientId())
                .orElseThrow(()->new RuntimeException("Patient Id Not Found"));
        PatientEpisodeEntity patientEpisodeEntity = PatientEpisodeConverter.toEntity(dto,patientInfoEntity,currentUser);
        PatientEpisodeDto patientEpisodeDto= PatientEpisodeConverter.toDTO(patientEpisodeRepo.save(patientEpisodeEntity));
        return patientEpisodeDto;
    }
}
