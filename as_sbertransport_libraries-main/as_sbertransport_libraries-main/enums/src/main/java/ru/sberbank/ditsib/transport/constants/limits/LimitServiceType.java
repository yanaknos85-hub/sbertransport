package ru.sberbank.ditsib.transport.constants.limits;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Типы лимитов.
 */
public enum LimitServiceType {

    /**
     * Пассажирский.
     */
    PASSENGER,

    /**
     * Грузовой.
     */
    CARGO,

    /**
     * Ремонт.
     */
    REPAIR;

    /**
     * Получение типа услуги лимита.
     *
     * @param transportTypeEnum тип транспорта.
     *
     * @return тип услуги релиза.
     */
    public static LimitServiceType getLimitServiceTypeByTransportType(TransportTypeEnum transportTypeEnum) {
        return switch (transportTypeEnum) {
            case COURIER, DEDICATED, INTERREGIONAL, DOMESTIC_COURIER, INDIVIDUAL -> CARGO;
            case TAXI, PERSONAL, PUBLIC, CARSHARING, BICYCLE, WALK, SCOOTER, GROUP_TRANSFER -> PASSENGER;
            case PRIVATE, SPECIAL, OFFICIAL -> REPAIR;
        };
    }
}
