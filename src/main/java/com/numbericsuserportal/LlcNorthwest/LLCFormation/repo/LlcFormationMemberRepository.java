package com.numbericsuserportal.LlcNorthwest.LLCFormation.repo;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LlcFormationMemberRepository extends JpaRepository<LlcFormationMember, Long> {
    List<LlcFormationMember> findByFormationIdOrderByIdAsc(Long formationId);
    void deleteByFormationId(Long formationId);
}

