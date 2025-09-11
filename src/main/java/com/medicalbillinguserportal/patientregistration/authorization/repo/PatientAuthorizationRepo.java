package com.medicalbillinguserportal.patientregistration.authorization.repo;

import com.medicalbillinguserportal.appointment.entity.AppointmentEntity;
import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientAuthorizationRepo extends JpaRepository<PatientAuthorizationEntity,Long> , JpaSpecificationExecutor<PatientAuthorizationEntity> {
    Optional<PatientAuthorizationEntity> findByIdAndIsActiveTrue(long id);
}
