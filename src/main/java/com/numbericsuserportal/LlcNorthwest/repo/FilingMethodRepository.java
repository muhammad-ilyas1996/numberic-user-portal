package com.numbericsuserportal.LlcNorthwest.repo;

import com.numbericsuserportal.LlcNorthwest.entity.FilingMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilingMethodRepository extends JpaRepository<FilingMethodEntity, Long> {
    Optional<FilingMethodEntity> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);
}

