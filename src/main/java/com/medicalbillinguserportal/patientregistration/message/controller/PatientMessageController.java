package com.medicalbillinguserportal.patientregistration.message.controller;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.service.PatientInsuranceService;
import com.medicalbillinguserportal.patientregistration.message.dto.PatientMessageDto;
import com.medicalbillinguserportal.patientregistration.message.service.PatientMessageService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/patient-message")
public class PatientMessageController {
    @Autowired
    public PatientMessageService patientMessageService;

    @PostMapping("/create")
    public ResponseEntity<PatientMessageDto> savePatientMessage(@RequestBody PatientMessageDto patientMessageDto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientMessageService.savePatientMessage(patientMessageDto,currentUser));
    }

}
