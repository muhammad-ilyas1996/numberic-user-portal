package com.numbericsuserportal.registration.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must be at least 10 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

