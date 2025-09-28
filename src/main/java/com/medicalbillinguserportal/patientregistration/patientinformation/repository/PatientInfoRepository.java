package com.medicalbillinguserportal.patientregistration.patientinformation.repository;

import com.medicalbillinguserportal.patientregistration.patientinformation.entity.PatientInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientInfoRepository extends JpaRepository<PatientInfoEntity, Long>, JpaSpecificationExecutor<PatientInfoEntity>{

    public PatientInfoEntity findById(long id);
}
