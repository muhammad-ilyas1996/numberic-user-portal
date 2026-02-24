package com.numbericsuserportal.recieptupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDataResponseDTO {
    
    private Long id;
    private Long receiptId;
    private Long userId;
    private String entryType;
    private String merchantName;
    private LocalDate receiptDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String rawText;
    private Double confidenceScore;
    private String category;
    private String status;
    private LocalDateTime extractedAt;
}
