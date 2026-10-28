package com.numbericsuserportal.federaltax.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "federal_tax_documents")
@Data
public class FederalTaxDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "stored_path", length = 500)
    private String storedPath;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "doc_type", length = 64)
    private String docType;

    @Lob
    @Column(name = "ocr_text", columnDefinition = "LONGTEXT")
    private String ocrText;

    @Lob
    @Column(name = "extracted_json", columnDefinition = "LONGTEXT")
    private String extractedJson;

    @Column(name = "uploaded_via", length = 32)
    private String uploadedVia;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
