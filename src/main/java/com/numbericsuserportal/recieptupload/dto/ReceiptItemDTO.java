package com.numbericsuserportal.recieptupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemDTO {
    
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
}
