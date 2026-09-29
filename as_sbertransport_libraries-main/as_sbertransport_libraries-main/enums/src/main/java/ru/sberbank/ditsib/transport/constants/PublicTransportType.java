package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


/**
 * Типы ОТ.
 */
@RequiredArgsConstructor
@Getter
public enum PublicTransportType {

    /**
     * Автобус
     */
    CITY_BUS("Автобус", PublicCompensationType.CITY_TRIP_COMPENSATION),

    /**
     * Троллейбус
     */
    CITY_TROLLEYBUS("Троллейбус", PublicCompensationType.CITY_TRIP_COMPENSATION),

    /**
     * Трамвай
     */
    CITY_TRAM("Трамвай", PublicCompensationType.CITY_TRIP_COMPENSATION),

    /**
     * Метро
     */
    CITY_METRO("Метро", PublicCompensationType.CITY_TRIP_COMPENSATION),


    /**
     * Междугородный автобус
     */
    SUBURB_BUS("Междугородный автобус", PublicCompensationType.SUBURB_TRIP_COMPENSATION),

    /**
     * Пригородный поезд
     */
    SUBURB_TRAIN("Пригородный поезд", PublicCompensationType.SUBURB_TRIP_COMPENSATION),

    /**
     * Паромная переправа
     */
    SUBURB_FERRY_CROSSING("Паромная переправа", PublicCompensationType.SUBURB_TRIP_COMPENSATION),

    /**
     * Междугородный троллейбус
     */
    SUBURB_TROLLEYBUS("Междугородный троллейбус", PublicCompensationType.SUBURB_TRIP_COMPENSATION),


    /**
     * Автобус - проездной
     */
    TRAVEL_CARD_BUS("Автобус", PublicCompensationType.TRAVEL_CARD_COMPENSATION),

    /**
     * Троллейбус - проездной
     */
    TRAVEL_CARD_TROLLEYBUS("Троллейбус", PublicCompensationType.TRAVEL_CARD_COMPENSATION),

    /**
     * Трамвай - проездной
     */
    TRAVEL_CARD_TRAM("Трамвай", PublicCompensationType.TRAVEL_CARD_COMPENSATION),

    /**
     * Метро - проездной
     */
    TRAVEL_CARD_METRO("Метро", PublicCompensationType.TRAVEL_CARD_COMPENSATION),

    /**
     * Единый - проездной
     */
    TRAVEL_CARD_ALL_CITY_TRANSPORT("Единый", PublicCompensationType.TRAVEL_CARD_COMPENSATION),

    /**
     * Платная парковка
     */
    PAID_PARKING("Платная парковка", PublicCompensationType.PAID_SERVICES_COMPENSATION),

    /**
     * Платная дорога
     */
    TOLL_ROAD("Платная дорога", PublicCompensationType.PAID_SERVICES_COMPENSATION),

    /**
     * Платная паромная переправа
     */
    PAID_FERRY_CROSSING("Переправа", PublicCompensationType.PAID_SERVICES_COMPENSATION);

    private final String rusName;
    private final PublicCompensationType publicCompensationType;


    /**
     * Получение типа ТС по имени.
     *
     * @param name название ТС.
     *
     * @return тип ТС.
     */
    public static Optional<PublicTransportType> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        for (PublicTransportType value : PublicTransportType.values()) {
            if (value.name().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}