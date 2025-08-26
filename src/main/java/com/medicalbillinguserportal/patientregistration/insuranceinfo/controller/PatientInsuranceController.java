package com.medicalbillinguserportal.patientregistration.insuranceinfo.controller;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.service.PatientInsuranceService;
import com.medicalbillinguserportal.patientregistration.patientinformation.service.PatientInfoService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/patient-insurance")
public class PatientInsuranceController {

    @Autowired
    public PatientInsuranceService patientInsuranceService;

    @PostMapping("/create")
    public ResponseEntity<PatientInsuranceDto> savePatientInsurance(@RequestBody PatientInsuranceDto patientInsuranceDto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientInsuranceService.savePatientInsurance(patientInsuranceDto,currentUser));
    }

}
