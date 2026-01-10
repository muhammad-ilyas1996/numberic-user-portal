package com.numbericsuserportal.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep4Dto {
    
    @NotBlank(message = "Owner full name is required")
    @Size(max = 200, message = "Owner full name must not exceed 200 characters")
    private String fullName;
    
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "^\\d{4}$", message = "SSN last 4 must be exactly 4 digits")
    private String ssnLast4;
    
    @Valid
    private AddressDto ownerAddress;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Ownership percent is required")
    @Min(value = 0, message = "Ownership percent must be at least 0")
    @Max(value = 100, message = "Ownership percent must be at most 100")
    private Double ownershipPercent;
    
    @AssertTrue(message = "SSN consent is required")
    private Boolean ssnConsent;
}

