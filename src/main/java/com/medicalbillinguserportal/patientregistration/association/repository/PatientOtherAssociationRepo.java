package com.medicalbillinguserportal.patientregistration.association.repository;


import com.medicalbillinguserportal.patientregistration.association.entity.PatientOtherAssociationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientOtherAssociationRepo extends JpaRepository<PatientOtherAssociationEntity,Long>, JpaSpecificationExecutor<PatientOtherAssociationEntity>
{
}
