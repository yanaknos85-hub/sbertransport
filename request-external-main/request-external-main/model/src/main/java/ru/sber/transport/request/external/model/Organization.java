package ru.sber.transport.request.external.model;

import java.util.List;
import java.util.UUID;

/**
 * Модель организации
 */
public interface Organization {

    /**
     * Идентификатор организации
     *
     * @return идентификатор организации
     */
    UUID getId();

    /**
     * Порядковый номер организации
     *
     * @return порядковый номер организации
     */
    long getDigitId();

    /**
     * Список доступных классов транспорта
     *
     * @return список доступных классов транспорта
     */
    List<String> getAvailableClasses();

}
