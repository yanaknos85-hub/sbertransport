package ru.sber.transport.trip.business.model;

import lombok.Getter;

/**
 * Типы инициаторов изменений по поездке
 */

@Getter
public enum Actor {

    /**
     * Диспетчер
     */
    DISPATCHER,

    /**
     * Водитель
     */
    DRIVER,

    /**
     * Пассажир
     */
    PASSENGER,

    /**
     * Система
     */
    SYSTEM,

    /**
     * Администратор
     */
    ADMIN,

    /**
     * Эпл
     */
    EWB

}
