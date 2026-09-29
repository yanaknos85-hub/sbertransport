package ru.sber.transport.trips.cargo.providers.checkin;

import ru.sber.transport.trips.cargo.business.model.Checkin;
import ru.sber.transport.trips.cargo.business.model.Trip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * Провайдер чекинов.
 */
public interface CheckinProvider {

    /**
     * Сохранение
     * @param checkin чекин
     */
    int save(Checkin checkin);

    /**
     * Получение
     * @param id ID чекинa
     */
    Optional<Checkin> get(UUID id);

    /**
     * Получение списка чекинов по ID поездки
     * @param tripId ID поездки
     */
    List<Checkin> findAllByTripId(UUID tripId);

    /**
     * Получение списка чекинов по списку ID поездок
     * @param trips поездки
     */
    List<Checkin> findAllByTripIds(List<Trip> trips);

}
