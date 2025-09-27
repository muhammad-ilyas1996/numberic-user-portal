package com.medicalbillinguserportal.patientregistration.patientinformation.controller;

import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.PatientInfoIdDto;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.search.PatientInfoSearch;
import com.medicalbillinguserportal.patientregistration.patientinformation.dto.search.PatientsSearch;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.service.PatientInfoService;
//import com.medicalbillinguserportal.queryrequest.dto.QueryRequestDTO;
//import com.medicalbillinguserportal.queryrequest.dto.request.QueryRequestSearch;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/Patient-info")
public class PatientInfoController {

    @Autowired
    private PatientInfoService patientInfoService;

    @PostMapping("/create")
    public ResponseEntity<PatientInfoDto> createPatientInfo(@RequestBody PatientInfoDto dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(patientInfoService.savePatientInfo(dto,currentUser));
    }

    //    @PostMapping("/view-detail")
//    public ResponseEntity<PatientInfoIdDto> getPatientIdInfo(@RequestBody PatientInfoIdDto dto)
//    {
//        return ResponseEntity.ok(patientInfoService.getPatientId(dto.getId()));
//    }
    @PostMapping("/getAll")
    public ResponseEntity<PatientInfoIdDto> getPatientIdInfo(@RequestBody PatientInfoIdDto dto)
    {
        return ResponseEntity.ok(patientInfoService.getPatientId(dto.getId()));
    }
    @PostMapping("/list")
    public ResponseEntity<Page<PatientInfoEntity>> getPatients(@RequestBody PatientsSearch requestDTO) {
        Page<PatientInfoEntity> requestPage = patientInfoService.searchPatients(requestDTO);
        return ResponseEntity.ok(requestPage);
    }

    @PostMapping("/view-detail")
    public ResponseEntity<PatientInfoDto> getFullPatientProfile(@RequestBody PatientInfoSearch patientInfoSearch)
    {
        PatientInfoDto dto= patientInfoService.getPatientAllDataById(patientInfoSearch.getId());
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

}
