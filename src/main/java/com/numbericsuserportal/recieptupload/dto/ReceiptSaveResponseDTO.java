package com.numbericsuserportal.recieptupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSaveResponseDTO {
    
    private Boolean success;
    private String message;
    private Long receiptId;
}
