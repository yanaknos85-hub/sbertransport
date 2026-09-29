package ru.sber.transport.trip.web.service;

import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;

public interface UpdateTripService {

    /**
     * Обновление поездки.
     *
     * @param driver водитель
     * @param trip поездка
     * @param tripStatus статус
     * @return поездка
     */
    Trip updateTrip(Driver driver, Trip trip, TripStatus tripStatus);


    /**
     * Увеличение счетчика автоназначения.
     *
     * @return поездка
     */
    Trip incrementAutoAssignCounter(Trip trip);

}
