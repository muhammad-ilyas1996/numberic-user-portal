package com.numbericsuserportal.ai.repo;

import com.numbericsuserportal.ai.entity.TaalrAgentSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaalrAgentSessionRepository extends JpaRepository<TaalrAgentSessionEntity, Long> {

    Optional<TaalrAgentSessionEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
