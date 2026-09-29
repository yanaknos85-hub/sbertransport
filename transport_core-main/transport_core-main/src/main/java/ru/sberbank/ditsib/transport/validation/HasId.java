package ru.sberbank.ditsib.transport.validation;

import java.util.UUID;

/**
 * Сущности для валидации должны имплементировать данный интерфейс для исключения проверяемого объекта из списка при
 * обновлении
 */
public interface HasId {

    /**
     * @return идентификатор.
     */
    UUID getId();

    /**
     * Установить идентификатор.
     *
     * @param id идентификатор.
     */
    void setId(UUID id);
}
