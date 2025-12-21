package com.numbericsuserportal.twilio.repository;

import com.numbericsuserportal.twilio.entity.TaxDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaxDocument entity
 */
@Repository
public interface TaxDocumentRepository extends JpaRepository<TaxDocument, Long> {

    /**
     * Find all documents for a specific tax case
     */
    List<TaxDocument> findByTaxCaseId(Long taxCaseId);

    /**
     * Count documents for a specific tax case
     */
    long countByTaxCaseId(Long taxCaseId);
}

