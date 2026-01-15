package com.numbericsuserportal.registration.dto;

import com.numbericsuserportal.registration.validation.UniqueEmail;
import com.numbericsuserportal.registration.validation.ValidPassword;
import com.numbericsuserportal.registration.validation.ValidPhone;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep1Dto {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @UniqueEmail
    private String email;
    
    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @ValidPhone
    private String phone;
    
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean termsAccepted;
    
    private Boolean marketingOptIn = false;
    
    @NotBlank(message = "Role selection is required")
    @Pattern(regexp = "^(Founder|Business_Owner|Accountant_Pro)$", 
             message = "Role must be one of: Founder, Business_Owner, Accountant_Pro")
    private String preferredRole;
    
    public enum AccountType {
        individual, business
    }
}

