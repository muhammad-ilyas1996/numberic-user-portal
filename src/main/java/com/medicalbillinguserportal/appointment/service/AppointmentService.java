package com.medicalbillinguserportal.appointment.service;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.usermanagement.domain.User;

public interface AppointmentService {

    AppointmentDto createAppointment(AppointmentDto dto, User currentUser);

}
