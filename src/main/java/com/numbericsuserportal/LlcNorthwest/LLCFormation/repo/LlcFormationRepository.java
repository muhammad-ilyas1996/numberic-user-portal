package com.numbericsuserportal.LlcNorthwest.LLCFormation.repo;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LlcFormationRepository extends JpaRepository<LlcFormation, Long> {
    Optional<LlcFormation> findByIdAndUserId(Long id, Long userId);
    List<LlcFormation> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LlcFormation> findByStatusIn(Collection<String> statuses);
}

