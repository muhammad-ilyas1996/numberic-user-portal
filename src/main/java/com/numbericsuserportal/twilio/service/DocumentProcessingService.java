package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.dto.OcrResult;

/**
 * Service interface for complete document processing pipeline
 * Orchestrates file handling, OCR, classification, and data extraction
 */
public interface DocumentProcessingService {
    
    /**
     * Process document from URL (download, OCR, classify, extract data)
     * 
     * @param documentUrl URL of the document (from Twilio media URL)
     * @param contentType Content type (e.g., "application/pdf", "image/jpeg")
     * @return OcrResult with extracted data and classification
     */
    OcrResult processDocument(String documentUrl, String contentType);
    
    /**
     * Convert extracted data object to JSON string for storage
     * 
     * @param extractedData Extracted data object
     * @return JSON string representation
     */
    String convertExtractedDataToJson(Object extractedData);
}

