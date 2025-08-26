package com.medicalbillinguserportal.patientregistration.association.repository;

import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientAssociationRepo extends JpaRepository<PatientAssociationEntity,Long> , JpaSpecificationExecutor<PatientAssociationEntity> {
}
