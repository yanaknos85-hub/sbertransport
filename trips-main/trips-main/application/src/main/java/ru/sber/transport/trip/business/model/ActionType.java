package ru.sber.transport.trip.business.model;

import lombok.Getter;

/**
 * Действия с поездкой
 */

@Getter
public enum ActionType {

    /**
     * Создание поездки
     */
    TRIP_CREATION,

    /**
     * Смена водителя
     */
    DRIVER_CHANGING,

    /**
     * Смена статуса поездки
     */
    STATUS_CHANGING,

    /**
     * Изменение фактических данных по поездке
     */
    FACT_DATA_CHANGING,

    /**
     * Некорректная попытка смены статуса
     */
    INVALID_STATUS_CHANGING_ATTEMPT,

    /**
     * Планирование поездки
     */
    TRIP_PLANNING,

    /**
     * Отмена планирования поездки
     */
    TRIP_PLANNING_CANCELLATION,

    /**
     * Замена ожидаемого тс
     */
    EXPECTED_VEHICLE_CHANGING,

    /**
     * Взятие поездки в работу диспетчером
     */
    DISPATCHER_TAKE_TO_WORK
}
