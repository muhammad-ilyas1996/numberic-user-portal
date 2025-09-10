package com.medicalbillinguserportal.patientregistration.authorization.controller;

import com.medicalbillinguserportal.appointment.dto.AppointmentRequestDetailDTO;
import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationRequestDetailDTO;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.authorization.service.PatientAuthorizationService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<PatientAuthorizationDto>>getAuthorizationList(@RequestBody PatientSearch requestDTO) {
        Page<PatientAuthorizationDto> patientAuthorizationEntity = patientAuthorizationService.searchPatientAuthorization(requestDTO);
        return ResponseEntity.ok(patientAuthorizationEntity);
    }

    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<PatientAuthorizationDto> getAuthorizationDetail(@RequestBody PatientAuthorizationRequestDetailDTO requestDTO) {
        return ResponseEntity.ok(patientAuthorizationService.getPatientAuthorizationtDetail(requestDTO.getId()));
    }
}
