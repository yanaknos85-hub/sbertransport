package ru.sber.transport.dispatcher.dto.enums;

import lombok.Getter;

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
     * Остановка в промежуточной точке маршрута.
     */
    INTERMEDIATE_WAYPOINT_ARRIVED,

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
