package com.medicalbillinguserportal.patientregistration.association.service;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PatientAssociationService {
    PatientAssociationDto saveAssociation(PatientAssociationDto dto, User currentUser);
    public Page<PatientAssociationDto> searchPatientAssociation(PatientSearch requestDTO);
    public PatientAssociationDto getPatientAssociationDetail(Long id);
}
