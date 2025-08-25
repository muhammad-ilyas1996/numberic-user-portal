package com.medicalbillinguserportal.appointment.service;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.appointment.dto.request.PatientSearch;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

public interface AppointmentService {

    AppointmentDto createAppointment(AppointmentDto dto, User currentUser);
    public Page<AppointmentEntity> searchAppointment(PatientSearch requestDTO);
    public AppointmentEntity getAppointmentDetail(Long id);
}
