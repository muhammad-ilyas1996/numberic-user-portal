package com.numbericsuserportal.recieptupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSaveRequestDTO {
    
    private Long receiptId; // For manual entry, user will enter this
    private Long userId; // Optional, for creating new receipt
    private String merchantName;
    private LocalDate date;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String category;
    private String status;
    private String entryType; // "MANUAL" or "OCR"
}
