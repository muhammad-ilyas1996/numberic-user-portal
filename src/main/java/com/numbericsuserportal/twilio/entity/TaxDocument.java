package com.numbericsuserportal.twilio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing tax documents uploaded via WhatsApp
 * Linked to TaxCase
 */
@Entity
@Table(name = "tax_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Foreign key to tax_cases table
     */
    @Column(name = "tax_case_id", nullable = false)
    private Long taxCaseId;

    /**
     * URL of the media/document from Twilio
     */
    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    /**
     * Content type of the media (e.g., image/jpeg, application/pdf)
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * Timestamp when the document was uploaded
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}

