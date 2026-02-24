package com.numbericsuserportal.recieptupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUploadResponseDTO {
    
    private Boolean success;
    private String message;
    private Long receiptId;
    private String merchantName;
    private LocalDate date;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String imageBase64;
    private Double confidenceScore;
    private String category;
    private String status;
}
