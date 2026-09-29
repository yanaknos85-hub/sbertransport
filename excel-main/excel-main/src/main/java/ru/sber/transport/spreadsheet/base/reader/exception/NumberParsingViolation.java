package ru.sber.transport.spreadsheet.base.reader.exception;

import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

import java.lang.annotation.Annotation;

/**
 * Ограничение парсинга данных.
 *
 * @param fieldName наименование поля.
 * @param value сработавшее значение.
 */
public record NumberParsingViolation(String fieldName,
                                     String value) implements CustomViolation {

    private static final String NOT_VALID_MESSAGE = "[%s] не является числом";

    @Override
    public String getMessage() {
        return getMessageTemplate();
    }

    @Override
    public String getMessageTemplate() {
        return NOT_VALID_MESSAGE.formatted(value);
    }

    @Override
    public Path getPropertyPath() {
        return new FieldPath(fieldName);
    }

    @Override
    public Object getInvalidValue() {
        return value;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return new NumberParsingViolationConstraintDescriptor(new ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation() { // NOSONAR anonymous annotation implementation. Cannot be a lambda

            @Override
            public Class<? extends Annotation> annotationType() {
                return ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation.class;
            }

        });
    }

    record NumberParsingViolationConstraintDescriptor(
        ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation violation) implements CustomConstraintDescriptor<ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation> {

        @Override
        public ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation getAnnotation() {
            return violation;
        }

    }
}
