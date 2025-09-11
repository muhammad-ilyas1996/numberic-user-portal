package com.medicalbillinguserportal.patientregistration.insuranceinfo.repo;

import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientInsuranceRepo extends JpaRepository<PatientInsuranceEntity,Long> , JpaSpecificationExecutor<PatientInsuranceEntity> {

    Optional<PatientInsuranceEntity> findByIdAndIsActiveTrue(long id);
    // List<PatientInfoEntity> findByPatientId(Long patientInfoId);
}
