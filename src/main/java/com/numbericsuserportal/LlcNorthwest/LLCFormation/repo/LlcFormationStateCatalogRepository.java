package com.numbericsuserportal.LlcNorthwest.LLCFormation.repo;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationStateCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LlcFormationStateCatalogRepository extends JpaRepository<LlcFormationStateCatalog, Long> {

    Optional<LlcFormationStateCatalog> findByStateCodeIgnoreCase(String stateCode);

    List<LlcFormationStateCatalog> findAllByActiveTrueOrderByStateNameAsc();

    boolean existsByStateCodeIgnoreCaseAndActiveTrue(String stateCode);
}
