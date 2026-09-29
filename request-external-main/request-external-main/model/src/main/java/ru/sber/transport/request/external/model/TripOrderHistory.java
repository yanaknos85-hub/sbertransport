package ru.sber.transport.request.external.model;

import java.time.OffsetDateTime;

/**
 * Интерфейс истории заказов поездок
 */
public interface TripOrderHistory {

    /**
     * Статус заявки
     *
     * @return Статус заявки
     */
    State getStatus();

    /**
     * Время изменения заявки
     *
     * @return Время изменения заявки
     */
    OffsetDateTime getModifiedAt();

    /**
     * Комментарий к заявке
     *
     * @return Комментарий к заявке
     */
    String getComment();

    /**
     * Причина изменения заявки
     *
     * @return Причина изменения заявки
     */
    String getReason();

}
