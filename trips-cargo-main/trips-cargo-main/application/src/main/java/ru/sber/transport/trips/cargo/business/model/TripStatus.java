package ru.sber.transport.trips.cargo.business.model;

import lombok.Getter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

@Getter
public enum TripStatus {

    /**
     * Поездка планируется
     */
    PLANNING(null, false),

    /**
     * Опубликовано.
     */
    SENT_TO_CONTRACTOR(0, false),

    /**
     * Ожидает назначения.
     */
    WAITING_FOR_ASSIGNMENT(1, false, TripRequestStatus.TAXI_AWAITING_SEARCH, TripRequestStatus.TAXI_APPROVED),

    /**
     * Водитель найден.
     */
    DRIVER_ASSIGNED(2, false, TripRequestStatus.TAXI_DRIVER_FOUND),

    /**
     * Водитель выехал.
     */
    DRIVER_ON_THE_WAY(4, false, TripRequestStatus.TAXI_DRIVER_ON_THE_WAY),

    /**
     * Водитель ожидает клиента.
     */
    DRIVER_ARRIVED(5, false, TripRequestStatus.TAXI_DRIVER_ARRIVED),

    /**
     * Поездка началась.
     */
    TRIP_IN_PROGRESS(6, false, TripRequestStatus.TAXI_TRIP_IN_PROGRESS),

    /**
     * Остановка в промежуточной точке маршрута.
     */
    INTERMEDIATE_WAYPOINT_ARRIVED(null, false),

    /**
     * Заказ выполнен.
     */
    ORDER_FINISHED(7, true, TripRequestStatus.TAXI_TRIP_FINISHED),

    /**
     * Клиент отменил заказ.
     */
    ORDER_CANCELLED_BY_CLIENT(8, true, TripRequestStatus.TAXI_CANCELLED),

    /**
     * Водитель отменил заказ.
     */
    ORDER_CANCELLED_BY_DRIVER(9,true),

    /**
     * Заказ просрочен.
     */
    ORDER_EXPIRED(10, true),

    /**
     * Неизвестный статус.
     */
    UNDEFINED(-1, false);

    private final boolean terminal;

    private final TripRequestStatus[] tripRequestStatuses;

    private final Integer code;

    TripStatus(Integer code, boolean terminal, TripRequestStatus... statuses) {
        this.code = code;
        this.terminal = terminal;
        tripRequestStatuses = statuses;
    }
}
