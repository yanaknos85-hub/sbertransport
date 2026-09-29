package ru.sber.transport.trips.cargo.business.model;

import lombok.Getter;

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
