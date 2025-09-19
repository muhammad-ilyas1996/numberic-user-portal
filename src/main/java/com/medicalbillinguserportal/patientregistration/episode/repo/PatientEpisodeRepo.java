package com.medicalbillinguserportal.patientregistration.episode.repo;

import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import com.medicalbillinguserportal.patientregistration.insuranceinfo.entity.PatientInsuranceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientEpisodeRepo extends JpaRepository<PatientEpisodeEntity,Long>, JpaSpecificationExecutor<PatientEpisodeEntity> {
    Optional<PatientEpisodeEntity> findByIdAndIsActiveTrue(long id);
}
