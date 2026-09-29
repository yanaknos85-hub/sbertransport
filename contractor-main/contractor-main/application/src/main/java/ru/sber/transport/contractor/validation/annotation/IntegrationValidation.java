package ru.sber.transport.contractor.validation.annotation;

import ru.sber.transport.contractor.validation.validator.IntegrationValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IntegrationValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationValidation {

    String message() default "Integration data wrong";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
