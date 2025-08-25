package com.medicalbillinguserportal.appointment.controller;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.appointment.dto.AppointmentRequestDetailDTO;
import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.appointment.service.AppointmentService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/create-appointment")
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody AppointmentDto dto, @AuthenticationPrincipal User currentUser)
    {
        return ResponseEntity.ok(appointmentService.createAppointment(dto,currentUser));
    }


    @PostMapping("/list")
    //  @PreAuthorize("hasAuthority('VIEW_PATIENT')")
    public ResponseEntity<Page<AppointmentEntity>> getProviderList(@RequestBody PatientSearch requestDTO) {
        Page<AppointmentEntity> appointmentEntities = appointmentService.searchAppointment(requestDTO);
        return ResponseEntity.ok(appointmentEntities);
    }

    @PostMapping("/view-detail")
    // @PreAuthorize("hasAuthority('VIEW_PROVIDER')")
    public ResponseEntity<AppointmentEntity> getProviderData(@RequestBody AppointmentRequestDetailDTO requestDTO) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetail(requestDTO.getId()));
    }
}
