package com.numbericsuserportal.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep3Dto {
    
    @NotNull(message = "Business address is required")
    @Valid
    private AddressDto businessAddress;
    
    @NotNull(message = "Mailing address same flag is required")
    private Boolean mailingAddressSame;
    
    @Valid
    private AddressDto mailingAddress; // Required if mailingAddressSame is false
}

