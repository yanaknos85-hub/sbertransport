package ru.sberbank.ditsib.transport.request.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.lang.annotation.*;

/**
 * Ограничение на статусы поездки
 */
@Documented
@Constraint(validatedBy = TripRequestStatusValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TripRequestStatusConstraint {
    
    /**
     * @return сообщение об ошибке.
     */
    String message() default "The trip request status is not allowed";
    
    /**
     * @return разрешенные статусы.
     */
    TripRequestStatus[] allowed() default {};
    
    /**
     * @return запрещенные статусы.
     */
    TripRequestStatus[] disallowed() default {};
    
    /**
     * @return группа валидации.
     */
    Class<?>[] groups() default {};
    
    /**
     * @return полезная нагрузка.
     */
    Class<? extends Payload>[] payload() default {};
}