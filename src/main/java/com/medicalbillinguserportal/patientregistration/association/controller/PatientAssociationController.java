package com.medicalbillinguserportal.patientregistration.association.controller;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationDto;
import com.medicalbillinguserportal.patientregistration.association.dto.PatientAssociationRequestDetailDTO;
import com.medicalbillinguserportal.patientregistration.association.service.PatientAssociationService;
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
@RequestMapping("/v2/patient-association")
public class PatientAssociationController {

    @Autowired
    public PatientAssociationService patientAssociationService;
    @PostMapping("/create")
    public ResponseEntity<PatientAssociationDto> savePatientAssociation(@RequestBody PatientAssociationDto patientAssociationDto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientAssociationService.savePatientAssociation(patientAssociationDto,currentUser));
    }
    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<PatientAssociationDto>> getAssociationList(@RequestBody PatientSearch requestDTO) {
        Page<PatientAssociationDto> patientAssociationDto = patientAssociationService.searchPatientAssociation(requestDTO);
        return ResponseEntity.ok(patientAssociationDto);
    }

    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<PatientAssociationDto> getAssociationDetail(@RequestBody PatientAssociationRequestDetailDTO requestDTO) {
        return ResponseEntity.ok(patientAssociationService.getPatientAssociationDetail(requestDTO.getId()));
    }
}
