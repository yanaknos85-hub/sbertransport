package ru.sber.transport.trips.cargo.business.model;

import lombok.Getter;

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
    TRIP_PLANNING_CANCELLATION
}

