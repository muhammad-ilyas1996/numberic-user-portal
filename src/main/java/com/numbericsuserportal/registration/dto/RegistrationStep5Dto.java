package com.numbericsuserportal.registration.dto;

import com.numbericsuserportal.registration.entity.BankAccount;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep5Dto {
    
    @NotBlank(message = "Account holder name is required")
    @Size(max = 200, message = "Account holder name must not exceed 200 characters")
    private String accountHolderName;
    
    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "Routing number must be exactly 9 digits")
    private String routingNumber;
    
    @NotBlank(message = "Account number is required")
    @Size(min = 4, max = 17, message = "Account number must be between 4 and 17 digits")
    private String accountNumber;
    
    @NotNull(message = "Account type is required")
    private BankAccount.AccountType accountType;
    
    private BankAccount.VerificationMethod verificationMethod;
}

