package com.medicalbillinguserportal.patientregistration.authorization.controller;

import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.service.PatientAuthorizationService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/patient-authorization")
public class PatientAuthorizationController {

    @Autowired
    public PatientAuthorizationService patientAuthorizationService;

    @PostMapping("/create")
    public ResponseEntity<PatientAuthorizationDto> savePatientAuthorization(@RequestBody PatientAuthorizationDto dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientAuthorizationService.savePatientAuthorization(dto,currentUser));
    }
}
