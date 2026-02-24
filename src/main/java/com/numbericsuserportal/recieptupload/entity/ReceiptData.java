package com.numbericsuserportal.recieptupload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "entry_type", length = 20)
    private String entryType;
    
    @Column(name = "merchant_name", length = 255)
    private String merchantName;
    
    @Column(name = "receipt_date")
    private LocalDate receiptDate;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "category", length = 255)
    private String category;
    
    @Column(name = "status", length = 255)
    private String status;
    
    @Column(name = "extracted_at", nullable = false)
    private LocalDateTime extractedAt;
    
    @PrePersist
    protected void onCreate() {
        if (extractedAt == null) {
            extractedAt = LocalDateTime.now();
        }
        if (entryType == null) {
            entryType = "OCR"; // Default to OCR
        }
    }
}
