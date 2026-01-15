package com.numbericsuserportal.registration.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Phone must be in E.164 format (e.g., +14155551212)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

