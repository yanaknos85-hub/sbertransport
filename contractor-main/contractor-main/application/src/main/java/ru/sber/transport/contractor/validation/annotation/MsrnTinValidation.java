package ru.sber.transport.contractor.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.sber.transport.contractor.validation.validator.MsrnTinValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MsrnTinValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MsrnTinValidation {

    String message() default "Msrn and\\or tin data wrong";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}