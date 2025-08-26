package com.medicalbillinguserportal.patientregistration.authorization.repo;

import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientAuthorizationRepo extends JpaRepository<PatientAuthorizationEntity,Long> , JpaSpecificationExecutor<PatientAuthorizationEntity> {
}
