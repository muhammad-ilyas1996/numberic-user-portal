package com.numbericsuserportal.federaltax.repo;

import com.numbericsuserportal.federaltax.entity.Form8879Authorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Form8879AuthorizationRepository extends JpaRepository<Form8879Authorization, Long> {
    List<Form8879Authorization> findByCaseIdOrderBySignedAtDesc(Long caseId);
}
