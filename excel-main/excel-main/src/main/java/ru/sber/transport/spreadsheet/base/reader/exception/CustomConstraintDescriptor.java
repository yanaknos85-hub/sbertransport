package ru.sber.transport.spreadsheet.base.reader.exception;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.Payload;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

interface CustomConstraintDescriptor<T extends Annotation> extends ConstraintDescriptor<T> {

    @Override
    default String getMessageTemplate() {
        return null;
    }

    @Override
    default Set<Class<?>> getGroups() {
        return Set.of();
    }

    @Override
    default Set<Class<? extends Payload>> getPayload() {
        return Set.of();
    }

    @Override
    default ConstraintTarget getValidationAppliesTo() {
        return null;
    }

    @Override
    default List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
        return List.of();
    }

    @Override
    default Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    default Set<ConstraintDescriptor<?>> getComposingConstraints() {
        return Set.of();
    }

    @Override
    default boolean isReportAsSingleViolation() {
        return false;
    }

    @Override
    default ValidateUnwrappedValue getValueUnwrapping() {
        return null;
    }

    @Override
    default <U> U unwrap(Class<U> type) {
        return null;
    }
}
