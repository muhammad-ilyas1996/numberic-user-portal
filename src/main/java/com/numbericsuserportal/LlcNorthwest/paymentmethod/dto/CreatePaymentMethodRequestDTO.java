package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new payment method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentMethodRequestDTO {
    
    /**
     * Card number
     */
    @NotBlank(message = "Card number is required")
    private String number;
    
    /**
     * Expiration month of payment method card
     */
    @NotBlank(message = "Expiration month is required")
    @JsonProperty("exp_month")
    private String expMonth;
    
    /**
     * Expiration year of payment method card
     */
    @NotBlank(message = "Expiration year is required")
    @JsonProperty("exp_year")
    private String expYear;
    
    /**
     * Card Verification Code (CVC) of the payment method card
     */
    @NotBlank(message = "CVC is required")
    private String cvc;
    
    /**
     * First name of the payment method card holder
     */
    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;
    
    /**
     * Last name of the payment method card holder
     */
    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;
    
    /**
     * Billing address of payment method card
     */
    @NotNull(message = "Billing address is required")
    @JsonProperty("billing_address")
    private BillingAddressDTO billingAddress;
}

