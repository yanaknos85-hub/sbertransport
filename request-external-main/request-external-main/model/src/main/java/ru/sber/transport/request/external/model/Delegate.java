package ru.sber.transport.request.external.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Модель делегата
 */
public interface Delegate {

    /**
     * Возвращает идентификатор делегата
     *
     * @return идентификатор делегата
     */
    UUID delegateId();

    /**
     * Возвращает идентификатор руководителя
     *
     * @return идентификатор руководителя
     */
    UUID supervisorId();

    /**
     * Возвращает дату начала действия делегирования
     *
     * @return дата начала действия делегирования
     */
    LocalDate startDate();

    /**
     * Возвращает дату окончания действия делегирования
     *
     * @return дата окончания действия делегирования
     */
    LocalDate endDate();

    /**
     * Возвращает статус делегирования
     *
     * @return статус делегирования
     */
    boolean active();

}
