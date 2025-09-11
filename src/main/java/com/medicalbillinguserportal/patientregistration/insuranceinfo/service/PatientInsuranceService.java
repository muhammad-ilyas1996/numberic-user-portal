package com.medicalbillinguserportal.patientregistration.insuranceinfo.service;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PatientInsuranceService {
    PatientInsuranceDto savePatientInsurance(PatientInsuranceDto dto, User currentUser);
    public Page<PatientInsuranceDto> searchPatientInsurance(PatientSearch requestDTO);
    public PatientInsuranceDto getPatientInsuranceDetail(Long id);
}
