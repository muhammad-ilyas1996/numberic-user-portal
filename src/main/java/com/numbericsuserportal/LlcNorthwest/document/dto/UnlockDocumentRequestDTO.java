package com.numbericsuserportal.LlcNorthwest.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for unlocking a document after payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockDocumentRequestDTO {
    
    /**
     * The id of the payment method
     */
    @NotNull(message = "Payment token is required")
    @JsonProperty("payment_token")
    private UUID paymentToken;
}

