package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaalrReceiptDraft {

    private Long receiptId;
    private String merchantName;
    private LocalDate date;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private Double confidenceScore;
}
