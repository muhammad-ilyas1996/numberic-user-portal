package com.numbericsuserportal.registration.dto;

import com.numbericsuserportal.registration.entity.BusinessProfile;
import com.numbericsuserportal.registration.validation.ValidEIN;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep2Dto {
    
    @Size(max = 255, message = "Business legal name must not exceed 255 characters")
    private String businessLegalName; // Required only for business accounts
    
    @Size(max = 255, message = "DBA must not exceed 255 characters")
    private String dba;
    
    private BusinessProfile.BusinessType businessType; // Required only for business accounts
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    @Size(max = 20, message = "NAICS code must not exceed 20 characters")
    private String naicsCode;
    
    @ValidEIN
    private String ein;
    
    private BusinessProfile.TaxClassification taxClassification;
    
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "Invalid website URL format")
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;
}

