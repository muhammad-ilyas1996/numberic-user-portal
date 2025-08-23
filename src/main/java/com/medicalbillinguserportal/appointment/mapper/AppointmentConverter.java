package com.medicalbillinguserportal.appointment.mapper;

import com.medicalbillinguserportal.appointment.dto.AppointmentDto;
import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.usermanagement.domain.User;

import java.util.Date;

public class AppointmentConverter {
    public static AppointmentDto toDTO(AppointmentEntity entity, User currentUser) {
        AppointmentDto dto = new AppointmentDto();
        dto.setAppointmentId(entity.getAppointmentId());
        dto.setAppointmentTime(entity.getAppointmentTime());
        dto.setType(entity.getType());
        dto.setResource(entity.getResource());
        dto.setLocation(entity.getLocation());
        dto.setPatientName(entity.getPatientName());
        dto.setPhone(entity.getPhone());
        dto.setDob(entity.getDob());
        dto.setNotesReason(entity.getNotesReason());
        dto.setFollowUp(entity.getFollowUp());
        dto.setAddReminderToRecallList(entity.getAddReminderToRecallList());
        dto.setNonChargeVisit(entity.getNonChargeVisit());
        dto.setTransitionOfCare(entity.getTransitionOfCare());

        //Audit
        dto.setCreatedBy(currentUser.getUserId().toString());
        dto.setCreatedOn(new Date());
        dto.setModifiedBy(currentUser.getUserId().toString());
        dto.setModifiedOn(new Date());

        return dto;
    }

    public static AppointmentEntity toEntity(AppointmentDto dto, User currentUser) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setAppointmentId(dto.getAppointmentId());
        entity.setAppointmentTime(dto.getAppointmentTime());
        entity.setType(dto.getType());
        entity.setResource(dto.getResource());
        entity.setLocation(dto.getLocation());
        entity.setPatientName(dto.getPatientName());
        entity.setPhone(dto.getPhone());
        entity.setDob(dto.getDob());
        entity.setNotesReason(dto.getNotesReason());
        entity.setFollowUp(dto.getFollowUp());
        entity.setAddReminderToRecallList(dto.getAddReminderToRecallList());
        entity.setNonChargeVisit(dto.getNonChargeVisit());
        entity.setTransitionOfCare(dto.getTransitionOfCare());

        //Audit
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());
        return entity;
    }
}
