package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OCR processing result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    
    /**
     * Document type detected: W-2, 1099-NEC, 1099-MISC, GOVERNMENT_ID, UNKNOWN
     */
    private String documentType;
    
    /**
     * Raw extracted text from OCR
     */
    private String rawText;
    
    /**
     * Extracted data based on document type
     * Can be W2ExtractedData, Form1099ExtractedData, or GovernmentIdExtractedData
     */
    private Object extractedData;
    
    /**
     * Overall confidence score
     */
    private Double overallConfidence;
    
    /**
     * Number of pages processed
     */
    private Integer pageCount;
    
    /**
     * Error message if processing failed
     */
    private String errorMessage;
    
    /**
     * Whether OCR processing was successful
     */
    private boolean success;
}

