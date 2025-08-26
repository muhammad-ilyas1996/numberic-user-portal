package com.medicalbillinguserportal.patientregistration.episode.repo;

import com.medicalbillinguserportal.patientregistration.authorization.entity.PatientAuthorizationEntity;
import com.medicalbillinguserportal.patientregistration.episode.entity.PatientEpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientEpisodeRepo extends JpaRepository<PatientEpisodeEntity,Long>, JpaSpecificationExecutor<PatientEpisodeEntity> {
}
