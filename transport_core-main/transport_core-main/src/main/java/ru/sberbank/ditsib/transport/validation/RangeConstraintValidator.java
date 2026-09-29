package ru.sberbank.ditsib.transport.validation;

import org.springframework.beans.BeanWrapperImpl;
import ru.sberbank.ditsib.transport.validation.annotation.Range;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для сравнения двух чисел при использовании аннотации @Range
 */
public class RangeConstraintValidator implements ConstraintValidator<Range, Object> {
    
    private String fromFieldName;
    
    private String toFieldName;
    
    private boolean exclude;
    
    @Override
    public void initialize(Range constraint) {
        this.fromFieldName = constraint.from();
        this.toFieldName = constraint.to();
        this.exclude = constraint.exclude();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (fromFieldName == null || toFieldName == null) {
            return true;
        }
        var from = (Comparable<Object>) new BeanWrapperImpl(value).getPropertyValue(fromFieldName);
        var to = (Comparable<Object>) new BeanWrapperImpl(value).getPropertyValue(toFieldName);
        if (from == null || to == null) {
            return true;
        }
        var result = from.compareTo(to);
        if (!exclude) {
            return result <= 0;
        } else {
            return result < 0;
        }
    }
}

