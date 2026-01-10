package com.numbericsuserportal.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep7Dto {
    
    private Boolean connectPlaid = false;
    
    private AccountingIntegration accountingIntegration;
    
    private Boolean consentToShareData = false;
    
    public enum AccountingIntegration {
        quickbooks, xero, netsuite, none
    }
}

