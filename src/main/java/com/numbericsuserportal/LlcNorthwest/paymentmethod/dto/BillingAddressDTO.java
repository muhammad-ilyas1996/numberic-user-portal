package com.numbericsuserportal.LlcNorthwest.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for billing address information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingAddressDTO {
    
    private String city;
    
    private String state;
    
    private String zip;
    
    private String country;
    
    @JsonProperty("address1")
    private String address1;
    
    @JsonProperty("address2")
    private String address2;
}

