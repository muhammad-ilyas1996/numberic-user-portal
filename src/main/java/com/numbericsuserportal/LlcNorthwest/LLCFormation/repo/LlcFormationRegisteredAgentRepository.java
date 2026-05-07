package com.numbericsuserportal.LlcNorthwest.LLCFormation.repo;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRegisteredAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LlcFormationRegisteredAgentRepository extends JpaRepository<LlcFormationRegisteredAgent, Long> {
    Optional<LlcFormationRegisteredAgent> findByFormationId(Long formationId);
    void deleteByFormationId(Long formationId);
}

