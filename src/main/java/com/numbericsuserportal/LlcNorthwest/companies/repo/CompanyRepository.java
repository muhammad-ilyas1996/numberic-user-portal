package com.numbericsuserportal.LlcNorthwest.companies.repo;

import com.numbericsuserportal.LlcNorthwest.companies.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);
    Optional<CompanyEntity> findByName(String name);
}

