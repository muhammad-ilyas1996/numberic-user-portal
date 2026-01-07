package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing payment method
 * All fields are optional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentMethodRequestDTO {
    
    /**
     * Card number
     */
    private String number;
    
    /**
     * Expiration month of payment method card
     */
    @JsonProperty("exp_month")
    private String expMonth;
    
    /**
     * Expiration year of payment method card
     */
    @JsonProperty("exp_year")
    private String expYear;
    
    /**
     * Card Verification Code (CVC) of the payment method card
     */
    private String cvc;
    
    /**
     * First name of the payment method card holder
     */
    @JsonProperty("first_name")
    private String firstName;
    
    /**
     * Last name of the payment method card holder
     */
    @JsonProperty("last_name")
    private String lastName;
    
    /**
     * Billing address of payment method card
     */
    @JsonProperty("billing_address")
    private BillingAddressDTO billingAddress;
}

