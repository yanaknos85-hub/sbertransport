package ru.sber.transport.request.external.model;

import java.util.UUID;

/**
 * Интерфейс фрода
 */
public interface Fraud {

    /**
     * Идентификатор заявки
     *
     * @return идентификатор заявки
     */
    UUID getId();

    /**
     * Комментарий к фродовой заявке
     *
     * @return комментарий к фродовой заявке
     */
    String getComment();

    /**
     * Тип фрода
     *
     * @return тип фрода
     */
    String getType();
}
