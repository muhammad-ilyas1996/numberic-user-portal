package com.numbericsuserportal.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep6Dto {
    
    private Boolean twoFactorEnabled = false;
    
    private TwoFactorMethod twoFactorMethod;
    
    public enum TwoFactorMethod {
        sms, authenticator, email
    }
}

