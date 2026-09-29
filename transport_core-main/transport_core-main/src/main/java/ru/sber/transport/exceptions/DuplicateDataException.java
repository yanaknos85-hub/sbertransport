package ru.sber.transport.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Map;

/**
 * Ошибка дублирующей сущности.
 */
@AllArgsConstructor
@Getter
public class DuplicateDataException extends RuntimeException {

    /**
     * Название сущности.
     */
    private final String entityName;

    /**
     * Конфликтные поля со значениями.
     */
    private final transient Map<String, Object> values;

    /**
     * Создать исключение.
     *
     * @param entityName название сущности.
     * @param field поле.
     * @param value значение.
     */
    public DuplicateDataException(@NonNull String entityName, @NonNull String field, @NonNull Serializable value) {
        this(entityName, Map.of(field, value));
    }

    /**
     * Создать исключение.
     *
     * @param entityClass название сущности.
     * @param values конфликтные значения.
     */
    public DuplicateDataException(@NonNull Class<?> entityClass, @NonNull Map<String, Object> values) {
        this(entityClass.getSimpleName(), values);
    }

    /**
     * Создать исключение.
     *
     * @param entityClass название сущности.
     * @param field поле.
     * @param value значение.
     */
    public DuplicateDataException(@NonNull Class<?> entityClass, @NonNull String field, @NonNull Serializable value) {
        this(entityClass.getSimpleName(), field, value);
    }

    @Override
    public String getMessage() {
        return "Conflict data on entity %s. Conflicted: %s".formatted(entityName, values);
    }
}