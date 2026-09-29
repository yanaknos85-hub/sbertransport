package ru.sber.transport.notifications.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public enum TripStatus {

    /**
     * Опубликовано.
     */
    SENT_TO_CONTRACTOR,

    /**
     * Ожидает назначения.
     */
    WAITING_FOR_ASSIGNMENT,

    /**
     * Водитель найден.
     */
    DRIVER_ASSIGNED,

    /**
     * Подтверждено водителем.
     */
    DRIVER_APPROVED,

    /**
     * Водитель выехал.
     */
    DRIVER_ON_THE_WAY,

    /**
     * Водитель ожидает клиента.
     */
    DRIVER_ARRIVED,

    /**
     * Поездка началась.
     */
    TRIP_IN_PROGRESS,

    /**
     * Заказ выполнен.
     */
    ORDER_FINISHED,

    /**
     * Клиент отменил заказ.
     */
    ORDER_CANCELLED_BY_CLIENT,

    /**
     * Водитель отменил заказ.
     */
    ORDER_CANCELLED_BY_DRIVER,

    /**
     * Заказ просрочен.
     */
    ORDER_EXPIRED,

    /**
     * Неизвестный статус.
     */
    UNDEFINED;
}
