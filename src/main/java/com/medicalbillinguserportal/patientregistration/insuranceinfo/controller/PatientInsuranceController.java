package com.medicalbillinguserportal.patientregistration.insuranceinfo.controller;

import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationDto;
import com.medicalbillinguserportal.patientregistration.authorization.dto.PatientAuthorizationRequestDetailDTO;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceDto;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.dto.PatientInsuranceRequestDetailDTO;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.service.PatientInsuranceService;
import com.medicalbillinguserportal.patientregistration.patientinformation.service.PatientInfoService;
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
@RequestMapping("/v2/patient-insurance")
public class PatientInsuranceController {

    @Autowired
    public PatientInsuranceService patientInsuranceService;

    @PostMapping("/create")
    public ResponseEntity<PatientInsuranceDto> savePatientInsurance(@RequestBody PatientInsuranceDto patientInsuranceDto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientInsuranceService.savePatientInsurance(patientInsuranceDto,currentUser));
    }
    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<PatientInsuranceDto>>getInsuranceList(@RequestBody PatientSearch requestDTO) {
        Page<PatientInsuranceDto> patientInsuranceDto = patientInsuranceService.searchPatientInsurance(requestDTO);
        return ResponseEntity.ok(patientInsuranceDto);
    }

    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<PatientInsuranceDto> getInsuranceDetail(@RequestBody PatientInsuranceRequestDetailDTO requestDTO) {
        return ResponseEntity.ok(patientInsuranceService.getPatientInsuranceDetail(requestDTO.getId()));
    }
}
