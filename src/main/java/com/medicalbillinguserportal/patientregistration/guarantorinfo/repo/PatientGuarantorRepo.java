package com.medicalbillinguserportal.patientregistration.guarantorinfo.repository;

import com.medicalbillinguserportal.patientregistration.guarantorinfo.entity.PatientGuarantorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientGuarantorRepo extends JpaRepository<PatientGuarantorEntity,Long>, JpaSpecificationExecutor<PatientGuarantorEntity> {

}
