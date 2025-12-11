package com.numbericsuserportal.LlcNorthwest.companies.repo;

import com.numbericsuserportal.LlcNorthwest.companies.entity.CompanyJurisdictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyJurisdictionRepository extends JpaRepository<CompanyJurisdictionEntity, Long> {
}

