package com.numbericsuserportal.twilio.repository;

import com.numbericsuserportal.twilio.entity.OcrExtractionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OcrExtractionResult entity
 */
@Repository
public interface OcrExtractionResultRepository extends JpaRepository<OcrExtractionResult, Long> {
    
    /**
     * Find OCR result by tax document ID
     */
    Optional<OcrExtractionResult> findByTaxDocumentId(Long taxDocumentId);
    
    /**
     * Find all OCR results for a specific tax case
     */
    List<OcrExtractionResult> findByTaxDocumentIdIn(List<Long> taxDocumentIds);
    
    /**
     * Find all OCR results by document type
     */
    List<OcrExtractionResult> findByDocumentType(String documentType);
    
    /**
     * Find all OCR results by processing status
     */
    List<OcrExtractionResult> findByProcessingStatus(String processingStatus);
}

