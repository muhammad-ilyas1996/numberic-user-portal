package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for payment methods API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodsResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private List<PaymentMethodDTO> result;
}

