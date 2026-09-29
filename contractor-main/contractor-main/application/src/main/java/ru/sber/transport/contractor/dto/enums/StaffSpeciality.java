package ru.sber.transport.contractor.dto.enums;

public enum StaffSpeciality
{
    /**
     * Диспетчер
     */
    DISPATCHER,

    /**
     * Менеджер
     */
    MANAGER,

    /**
     * Водитель пассажирских перевозок
     */
    PASSENGER_DRIVER,

    /**
     * Водитель грузовых перевозок
     */
    CARGO_DRIVER;

    public enum DriverStaffSpeciality {

        /**
         * Пассажирские перевозки
         */
        PASSENGER,

        /**
         * Грузовые перевозки
         */
        CARGO;

    }
}
