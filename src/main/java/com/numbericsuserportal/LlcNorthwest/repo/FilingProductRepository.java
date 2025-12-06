package com.numbericsuserportal.LlcNorthwest.repo;

import com.numbericsuserportal.LlcNorthwest.entity.FilingProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilingProductRepository extends JpaRepository<FilingProductEntity, Long> {
    Optional<FilingProductEntity> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);
}

