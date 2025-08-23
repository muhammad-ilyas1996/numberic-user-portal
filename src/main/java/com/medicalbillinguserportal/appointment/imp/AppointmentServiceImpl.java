package com.medicalbillinguserportal.appointment.imp;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.appointment.mapper.AppointmentConverter;
import com.medicalbillinguserportal.appointment.repo.AppointmentRepo;
import com.medicalbillinguserportal.appointment.service.AppointmentService;
import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;
    @Override
    public AppointmentDto createAppointment(AppointmentDto dto, User currentUser)
    {
        AppointmentEntity appointmentEntity= AppointmentConverter.toEntity(dto,currentUser);
        AppointmentEntity saved = appointmentRepo.save(appointmentEntity);
        return AppointmentConverter.toDTO(saved,currentUser);
    }
}
