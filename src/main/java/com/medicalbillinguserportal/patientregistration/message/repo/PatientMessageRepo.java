package com.medicalbillinguserportal.patientregistration.message.repo;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.message.entity.PatientMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientMessageRepo extends JpaRepository<PatientMessageEntity,Long>, JpaSpecificationExecutor<PatientMessageEntity> {
}
