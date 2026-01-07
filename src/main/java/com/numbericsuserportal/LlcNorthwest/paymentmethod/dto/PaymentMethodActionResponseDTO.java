package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment method create/update/delete actions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodActionResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private ActionResult result;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionResult {
        @JsonProperty("status_code")
        private Integer statusCode;
        
        private Boolean success;
    }
}

