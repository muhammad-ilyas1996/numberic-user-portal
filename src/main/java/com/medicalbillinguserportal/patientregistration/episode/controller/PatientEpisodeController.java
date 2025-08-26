package com.medicalbillinguserportal.patientregistration.episode.controller;


import com.medicalbillinguserportal.patientregistration.episode.dto.PatientEpisodeDto;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.episode.service.PatientEpisodeService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/patient-episode")
public class PatientEpisodeController {

    @Autowired
    public PatientEpisodeService patientEpisodeService;

    @PostMapping("/create")
    public ResponseEntity<PatientEpisodeDto> savepatientEpisode(@RequestBody PatientEpisodeDto dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientEpisodeService.savePatientEpisode(dto,currentUser));
    }
}
