package ru.sber.transport.request.external.business.exception;

import lombok.Getter;

/**
 * Исключение, возникающее при конфликте данных.
 */
@Getter
public class DataConflictException extends RuntimeException {

    /**
     * Класс сущности, для которой возник конфликт.
     */
    private final Class<?> entityClass;

    /**
     * Поле, в котором возник конфликт.
     */
    private final String conflictedField;

    /**
     * Старое значение поля.
     */
    private final Object oldValue;

    /**
     * Новое значение поля.
     */
    private final Object newValue;

    /**
     * Создать исключение конфликта данных.
     * @param entityClass Класс сущности, для которой возник конфликт.
     * @param conflictedField Поле, в котором возник конфликт.
     * @param oldValue Старое значение поля.
     * @param newValue Новое значение поля.
     */
    public DataConflictException(Class<?> entityClass, String conflictedField, Object oldValue, Object newValue) {
        super("Data conflict. Entity: %s. Field: %s. Old value: %s. New value: %s".formatted(entityClass.getSimpleName(), conflictedField, oldValue, newValue));
        this.entityClass = entityClass;
        this.conflictedField = conflictedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
