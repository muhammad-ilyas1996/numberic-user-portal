package com.medicalbillinguserportal.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDto {
    private Long appointmentId;
    private LocalDateTime appointmentTime;
    private String type;
    private String resource;
    private String location;
    private String patientName;
    private String phone;
    private LocalDateTime dob;
    private String notesReason;
    private String followUp;
    private Boolean addReminderToRecallList;
    private Boolean nonChargeVisit;
    private String transitionOfCare;

    //Base Entity
    private Date createdOn;
    private Date modifiedOn;
    private String createdBy;
    private String modifiedBy;
    private Boolean isActive;
}
