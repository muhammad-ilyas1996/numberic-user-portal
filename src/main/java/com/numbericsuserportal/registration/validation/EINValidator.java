package com.numbericsuserportal.registration.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EINValidator implements ConstraintValidator<ValidEIN, String> {
    
    private static final Pattern EIN_FORMATTED = Pattern.compile("^\\d{2}-\\d{7}$");
    private static final Pattern EIN_UNFORMATTED = Pattern.compile("^\\d{9}$");
    
    @Override
    public boolean isValid(String ein, ConstraintValidatorContext context) {
        if (ein == null || ein.trim().isEmpty()) {
            return true; // Optional field
        }
        String cleaned = ein.replaceAll("[\\s-]", "");
        return EIN_FORMATTED.matcher(ein).matches() || EIN_UNFORMATTED.matcher(cleaned).matches();
    }
}

