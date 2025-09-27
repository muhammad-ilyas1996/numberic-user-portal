package com.medicalbillinguserportal.patientregistration.association.repository;

import com.medicalbillinguserportal.patientregistration.association.entity.PatientAssociationEntity;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientAssociationRepo extends JpaRepository<PatientAssociationEntity,Long> , JpaSpecificationExecutor<PatientAssociationEntity> {
    Optional<PatientAssociationEntity> findByIdAndIsActiveTrue(long id);
}
