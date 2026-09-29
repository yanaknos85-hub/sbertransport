package ru.sber.transport.exceptions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Ошибка отсутствующей сущности.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EntityNotFoundException extends RuntimeException {

    /**
     * Название искомой сущности.
     */
    private final String entityName;

    /**
     * Идентификатор искомой сущности.
     */
    private final transient Object entityId;

    /**
     * Признак необходимости вывода стактрейса
     */
    private final boolean stackTraceEnabled;

    /**
     * Создать исключение.
     *
     * @param entityClass Искомая сущность.
     * @param entityId Искомый идентификатор.
     */
    public EntityNotFoundException(@NonNull Class<?> entityClass, @NonNull Object entityId) {
        this(entityClass.getSimpleName(), entityId, true);
    }

    /**
     * Создать исключение.
     *
     * @param entityClass Искомая сущность.
     * @param entityId Искомый идентификатор.
     * @param stackTraceEnabled Признак необходимости вывода стактрейса
     */
    public EntityNotFoundException(@NonNull Class<?> entityClass, @NonNull Object entityId, boolean stackTraceEnabled) {
        this(entityClass.getSimpleName(), entityId, stackTraceEnabled);
    }

    @Override
    public String getMessage() {
        return "Data not found: Entity: %s, ID: %s".formatted(entityName, entityId);
    }
}