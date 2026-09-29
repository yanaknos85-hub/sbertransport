package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.Route;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работыт с маршрутами.
 */
public interface RouteRepository extends JooqRepository<Route, RouteRecord, UUID> {

    /**
     * Поиск всех маршрута по трипу.
     *
     * @param tripId     идентификатор трипа.
     * @param sourceType источник данных.
     * @return список координат.
     */
    Optional<RouteRecord> findByTripIdAndSource(UUID tripId, RouteSource sourceType);

    /**
     * Save the route if it does not exist.
     *
     * @param routeRecord
     */
    void insertIfNotExist(RouteRecord routeRecord);
}
