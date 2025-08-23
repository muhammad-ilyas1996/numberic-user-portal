package com.medicalbillinguserportal.appointment.repo;

import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AppointmentRepo extends JpaRepository<AppointmentEntity, Long >, JpaSpecificationExecutor<AppointmentEntity> {

}
