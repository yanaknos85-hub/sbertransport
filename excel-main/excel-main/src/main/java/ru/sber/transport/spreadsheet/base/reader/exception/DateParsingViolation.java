package ru.sber.transport.spreadsheet.base.reader.exception;

import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import ru.sber.transport.spreadsheet.base.reader.annotation.DataParsingViolation;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * Ограничение парсинга данных.
 *
 * @param fieldName наименование поля.
 * @param value сработавшее значение.
 */
public record DateParsingViolation(String fieldName,
                                   Class<?> fieldType,
                                   String value) implements CustomViolation {

    private static final String NOT_VALID = "[%s] не является %s";
    private static final Map<Class<?>, String> FIELD_TYPE_PARSE_MAP = Map.of(
            LocalDateTime.class, "датой временем",
            LocalTime.class, "временем",
            LocalDate.class, "датой"
    );

    @Override
    public String getMessage() {
        return getMessageTemplate();
    }

    @Override
    public String getMessageTemplate() {
        return NOT_VALID.formatted(value, parseFieldType());
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
    @SuppressWarnings("java:S1604")
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return new DataParsingViolationConstraintDescriptor(new DataParsingViolation() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DataParsingViolation.class;
            }

        });
    }

    private String parseFieldType() {
        if (!FIELD_TYPE_PARSE_MAP.containsKey(fieldType)) {
            return fieldType.getSimpleName();
        }

        return FIELD_TYPE_PARSE_MAP.get(fieldType);
    }

    record DataParsingViolationConstraintDescriptor(DataParsingViolation violation) implements CustomConstraintDescriptor<DataParsingViolation> {

        @Override
        public DataParsingViolation getAnnotation() {
            return violation;
        }
    }
}
