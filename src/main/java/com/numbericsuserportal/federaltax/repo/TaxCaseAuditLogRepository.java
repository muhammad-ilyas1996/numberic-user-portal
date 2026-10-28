package com.numbericsuserportal.federaltax.repo;

import com.numbericsuserportal.federaltax.entity.TaxCaseAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxCaseAuditLogRepository extends JpaRepository<TaxCaseAuditLog, Long> {
    List<TaxCaseAuditLog> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
