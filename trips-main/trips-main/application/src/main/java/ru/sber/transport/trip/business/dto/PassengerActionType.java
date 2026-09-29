package ru.sber.transport.trip.business.dto;

/**
 * Действия пассажира на точке маршрута
 */
public enum PassengerActionType {
    /**
     * Посадка
     */
    BOARDING,

    /**
     * Высадка
     */
    UNBOARDING,

    /**
     * Ожидание
     */
    WAIT
}
