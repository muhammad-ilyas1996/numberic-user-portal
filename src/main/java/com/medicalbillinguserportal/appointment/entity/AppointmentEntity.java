package com.medicalbillinguserportal.appointment.entity;

import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;
    private LocalDateTime appointmentTime;
    private String type;
    private String resource;
    private String location;
    private String patientName;
    private String phone;
    private LocalDateTime dob;
    @Column(length = 2000)
    private String notesReason;
    @Column(length = 255)
    private String followUp;
    private Boolean addReminderToRecallList = false;
    private Boolean nonChargeVisit = false;
    private String transitionOfCare;
}
