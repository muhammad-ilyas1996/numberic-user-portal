package com.numbericsuserportal.taxintake.entity;

import com.numbericsuserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proconnect_tax_intake_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProconnectTaxIntakeRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "software_name", nullable = false, length = 40)
    private String softwareName = "PROCONNECT";

    @Column(name = "original_file_name", nullable = false, length = 260)
    private String originalFileName;

    @Column(name = "stored_file_path", nullable = false, length = 500)
    private String storedFilePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "extracted_data_json", columnDefinition = "TEXT")
    private String extractedDataJson;

    @Column(name = "overall_confidence")
    private Double overallConfidence;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "processing_status", length = 20)
    private String processingStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
