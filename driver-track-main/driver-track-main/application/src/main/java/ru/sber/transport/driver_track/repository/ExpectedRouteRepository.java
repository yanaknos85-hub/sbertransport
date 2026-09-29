package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.ExpectedRoute;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.dto.RouteSource;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работыт с маршрутами.
 */
public interface ExpectedRouteRepository extends JooqRepository<ExpectedRoute, ExpectedRouteRecord, UUID> {

    /**
     * Поиск всех маршрута по трипу.
     *
     * @param tripId     идентификатор трипа.
     * @param sourceType источник данных.
     * @return список координат.
     */
    Optional<ExpectedRouteRecord> findByTripIdAndSource(UUID tripId, RouteSource sourceType);

    /**
     * Save the route if it does not exist.
     *
     * @param expectedRouteRecord
     */
    void insertIfNotExist(ExpectedRouteRecord expectedRouteRecord);
}
