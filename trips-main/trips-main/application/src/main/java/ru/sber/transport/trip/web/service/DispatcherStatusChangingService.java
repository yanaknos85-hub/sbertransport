package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;

public interface DispatcherStatusChangingService {

    /**
     * Смена статуса по поездке диспетчером и выолнение соответствующих действий с данными водителя и смены
     * @param trip поездка
     * @param status статус поездки
     * @return измененная поездка
     */
    Trip processStatusChangingByDispatcher(Trip trip, TripStatus status);

    /**
     * Проверка доступности обновления статуса
     * @param trip поездка
     * @param driver подитель
     */
    void validateUpdate(Trip trip, Driver driver);

}
