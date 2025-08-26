package com.medicalbillinguserportal.patientregistration.guarantorinfo.controller;

import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.service.PatientEpisodeService;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.dto.PatientGuarantorDto;
import com.medicalbillinguserportal.patientregistration.guarantorinfo.service.PatientGuarantorService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/patient-guarantor")
public class PatientGuarantorController {
    @Autowired
    public PatientGuarantorService patientGuarantorService;

    @PostMapping("/create")
    public ResponseEntity<PatientGuarantorDto> savepatientGuarantor(@RequestBody PatientGuarantorDto dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientGuarantorService.savePatientGuarantor(dto,currentUser));
    }
}
