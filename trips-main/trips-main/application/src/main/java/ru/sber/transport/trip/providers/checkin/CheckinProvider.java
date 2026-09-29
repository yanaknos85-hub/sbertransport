package ru.sber.transport.trip.providers.checkin;

import ru.sber.transport.trip.business.model.Checkin;
import ru.sber.transport.trip.business.model.TripStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * @param tripIds список ID поездок
     */
    List<Checkin> findAllByTripIds(List<UUID> tripIds);

    /**
     * Получение списка чекинов по списку ID поездок и статусу
     * @param tripIds список ID поездок
     * @param status статус
     */
    List<Checkin> findAllByTripIdsAndStatus(List<UUID> tripIds, TripStatus status);

}
