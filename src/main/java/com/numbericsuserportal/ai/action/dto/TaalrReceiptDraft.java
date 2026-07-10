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
    private String category;
    /** OCR or MANUAL */
    private String entryType = "OCR";
    /** When true, next free-text answers edit fields before save. */
    private boolean editing;
    private String editField;
}
