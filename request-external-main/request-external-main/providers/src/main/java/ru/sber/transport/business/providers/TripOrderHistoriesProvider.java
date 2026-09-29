package ru.sber.transport.business.providers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderHistory;

/**
 * Провайдер истории заказов поездок.
 */
public interface TripOrderHistoriesProvider {

    /**
     * Сохранить историю заказа поездки.
     *
     * @param orderId    Идентификатор заказа поездки.
     * @param status     Статус заявки.
     * @param comment    Комментарий к заявке.
     * @param modifiedBy Идентификатор пользователя, который изменил заявку.
     * @param reason     Причина изменения статуса заявки.
     * @param receipt    Название файла чека
     * @param approver   Сотрудник, который подтвердил заявку.
     */
    void save(UUID orderId, State status, String comment, UUID modifiedBy, String reason, String receipt, UUID approver);

    /**
     * Получить историю заказа поездки.
     *
     * @param orderIds Идентификаторы заказов поездок.
     * @return История заказов поездок.
     */
    Map<UUID, List<TripOrderHistory>> get(List<UUID> orderIds);

    /**
     * Получить историю изменения статусов заказа поездки.
     *
     * @param orderId Идентификатор заказа поездки.
     * @return История изменения статусов заказа поездки.
     */
    List<TripOrderHistory> get(UUID orderId);
}
