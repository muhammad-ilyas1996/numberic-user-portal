package com.numbericsuserportal.twilio.service;

/**
 * Service interface for document classification
 * Classifies documents into: W-2, 1099-NEC, 1099-MISC, GOVERNMENT_ID, UNKNOWN
 */
public interface DocumentClassifierService {
    
    /**
     * Classify document type based on extracted OCR text
     * 
     * @param extractedText Raw text extracted from OCR
     * @return Document type: W-2, 1099-NEC, 1099-MISC, GOVERNMENT_ID, or UNKNOWN
     */
    String classifyDocument(String extractedText);
    
    /**
     * Get confidence score for classification
     * 
     * @param extractedText Raw text extracted from OCR
     * @param documentType Classified document type
     * @return Confidence score (0.0 to 1.0)
     */
    Double getClassificationConfidence(String extractedText, String documentType);
}

