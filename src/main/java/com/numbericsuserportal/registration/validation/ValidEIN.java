package com.numbericsuserportal.registration.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EINValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEIN {
    String message() default "EIN must be in format XX-XXXXXXX or 9 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

