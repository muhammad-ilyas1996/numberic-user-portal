package com.medicalbillinguserportal.patientregistration.episode.service;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.mapper.PatientAuthorizationConverter;
import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.episode.mapper.PatientEpisodeConverter;
import com.medicalbillinguserportal.patientregistration.episode.repo.PatientEpisodeRepo;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.repository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PatientEpisodeService {
    PatientEpisodeDto savePatientEpisode(PatientEpisodeDto dto, User currentUser);
    public Page<PatientEpisodeDto> searchPatientEpisode(PatientSearch requestDTO);
    public PatientEpisodeDto getPatientEpisodeDetail(Long id);
    PatientEpisodeDto updateEpisode(PatientEpisodeDto dto, User currentUser);
}


