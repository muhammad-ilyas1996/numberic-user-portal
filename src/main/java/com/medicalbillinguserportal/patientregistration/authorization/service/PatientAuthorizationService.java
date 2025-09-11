package com.medicalbillinguserportal.patientregistration.authorization.service;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;
public interface PatientAuthorizationService {
    PatientAuthorizationDto savePatientAuthorization(PatientAuthorizationDto dto, User currentUser);
    public Page<PatientAuthorizationDto> searchPatientAuthorization(PatientSearch requestDTO);
    public PatientAuthorizationDto getPatientAuthorizationtDetail(Long id);
}
