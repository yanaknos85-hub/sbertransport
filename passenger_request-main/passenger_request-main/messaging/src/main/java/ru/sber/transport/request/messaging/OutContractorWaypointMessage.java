package ru.sber.transport.request.messaging;

import ru.sberbank.ditsib.transport.constants.TaxiStopType;

import java.time.Duration;

/**
 * Информация о точке маршрута
 *
 * @param id           Порядковый номер точки маршрута
 * @param type         Тип(посадка, высадка, ожидание)
 * @param address      Адрес точки маршрута
 * @param latitude     Широта
 * @param longitude    Долгота
 * @param passengersId Порядковый номер пассажира
 * @param timeWait     Время ожидания в точке маршрута
 */

public record OutContractorWaypointMessage(
        String id,
        TaxiStopType type,
        String address,
        String latitude,
        String longitude,
        String passengersId,
        Duration timeWait
) {
}
