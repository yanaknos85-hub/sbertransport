package ru.sber.transport.contractor.validation.validator;

import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import jakarta.validation.Validator;

public class ShiftEmptyValidator extends SpringValidatorAdapter {
    /**
     * Create a new SpringValidatorAdapter for the given JSR-303 Validator.
     * Нужен для игнорирования стандартной валидации, т.к. при текущей реализации
     * смен водителей результаты неудачной валидации должны попадать в ответ на
     * запрос.
     *
     * @param targetValidator the JSR-303 Validator to wrap
     */
    public ShiftEmptyValidator(Validator targetValidator) {
        super(targetValidator);
    }
}
