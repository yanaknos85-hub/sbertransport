package ru.sber.transport.request.external.model;

import java.util.List;
import java.util.UUID;

/**
 * Бизнес-объект должности сотрудника
 */
public interface Position {

    /**
     * Получить идентификатор должности
     *
     * @return идентификатор должности
     */
    UUID getId();

    /**
     * Получить список доступных классов транспорта
     *
     * @return список доступных классов транспорта
     */
    List<String> getAvailableClasses();

}
