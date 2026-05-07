package com.numbericsuserportal.LlcNorthwest.LLCFormation.repo;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LlcFormationRateRepository extends JpaRepository<LlcFormationRate, Long> {
    List<LlcFormationRate> findByActiveTrue();
    Optional<LlcFormationRate> findByRateTypeAndStateCodeAndSpeedCode(String rateType, String stateCode, String speedCode);
}

