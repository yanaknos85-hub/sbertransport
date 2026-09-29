package ru.sber.transport.dispatcher.service;

import ru.sber.transport.dispatcher.database.model.Trip;

import java.util.UUID;

public interface TripsService {

    /**
     * Сохранение поездки
     * @param trip поездка
     * @return сохраненная поездка
     */
    Trip save(Trip trip);

    /**
     * Удаление поездки
     * @param tripId поездка
     */
    void delete(UUID tripId);

}
