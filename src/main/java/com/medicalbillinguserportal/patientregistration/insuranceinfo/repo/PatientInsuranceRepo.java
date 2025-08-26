package com.medicalbillinguserportal.patientregistration.insuranceinfo.repository;

import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientInsuranceRepo extends JpaRepository<PatientInsuranceEntity,Long> , JpaSpecificationExecutor<PatientInsuranceEntity> {

    // List<PatientInfoEntity> findByPatientId(Long patientInfoId);
}
