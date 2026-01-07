package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for payment method card information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {
    
    /**
     * Unique identifier for payment method card
     */
    private UUID id;
    
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
     * Last 4 digits of the payment method card number
     */
    private String last4;
    
    /**
     * Expiration month of payment method card
     */
    @JsonProperty("exp_month")
    private Integer expMonth;
    
    /**
     * Expiration year of payment method card
     */
    @JsonProperty("exp_year")
    private Integer expYear;
    
    /**
     * Date of creation for the saved payment method card
     */
    @JsonProperty("created_at")
    private String createdAt;
    
    /**
     * The brand of the payment method card (VISA, MASTERCARD, etc.)
     */
    private String brand;
    
    /**
     * Whether or not payment method is a prepaid card
     */
    @JsonProperty("is_prepaid")
    private Boolean isPrepaid;
    
    /**
     * Billing address of payment method card
     */
    @JsonProperty("billing_address")
    private BillingAddressDTO billingAddress;
}

