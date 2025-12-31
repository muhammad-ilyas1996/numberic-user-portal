package com.numbericsuserportal.twilio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing OCR extraction results
 */
@Entity
@Table(name = "ocr_extraction_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrExtractionResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Foreign key to tax_documents table
     */
    @Column(name = "tax_document_id", nullable = false)
    private Long taxDocumentId;
    
    /**
     * Document type: W-2, 1099-NEC, 1099-MISC, GOVERNMENT_ID, UNKNOWN
     */
    @Column(name = "document_type", length = 50)
    private String documentType;
    
    /**
     * Raw OCR text (stored as TEXT for large content)
     */
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;
    
    /**
     * Extracted data as JSON (stored as TEXT)
     * Contains structured data based on document type
     */
    @Column(name = "extracted_data_json", columnDefinition = "TEXT")
    private String extractedDataJson;
    
    /**
     * Overall confidence score (0.0 to 1.0)
     */
    @Column(name = "overall_confidence")
    private Double overallConfidence;
    
    /**
     * Number of pages processed
     */
    @Column(name = "page_count")
    private Integer pageCount;
    
    /**
     * Processing status: SUCCESS, FAILED, PENDING
     */
    @Column(name = "processing_status", length = 20)
    private String processingStatus;
    
    /**
     * Error message if processing failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Timestamp when OCR processing was completed
     */
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
        if (processingStatus == null) {
            processingStatus = "PENDING";
        }
    }
}

