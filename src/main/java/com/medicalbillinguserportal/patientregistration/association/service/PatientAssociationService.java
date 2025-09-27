package com.medicalbillinguserportal.patientregistration.association.service;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.association.mapper.PatientAssociationConverter;
import com.medicalbillinguserportal.patientregistration.association.repository.PatientAssociationRepo;
import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.respository.PatientInfoRepository;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PatientAssociationService {
    PatientAssociationDto savePatientAssociation(PatientAssociationDto dto, User currentUser);
    public Page<PatientAssociationDto> searchPatientAssociation(PatientSearch requestDTO);
    public PatientAssociationDto getPatientAssociationDetail(Long id);
}
