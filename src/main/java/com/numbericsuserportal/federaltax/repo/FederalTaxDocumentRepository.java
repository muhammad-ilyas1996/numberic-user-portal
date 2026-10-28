package com.numbericsuserportal.federaltax.repo;

import com.numbericsuserportal.federaltax.entity.FederalTaxDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FederalTaxDocumentRepository extends JpaRepository<FederalTaxDocument, Long> {
    List<FederalTaxDocument> findByCaseIdOrderByCreatedAtDesc(Long caseId);
}
