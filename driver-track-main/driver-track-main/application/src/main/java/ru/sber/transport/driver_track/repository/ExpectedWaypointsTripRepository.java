package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.ExpectedWaypointsTrip;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с плановыми точками маршрута.
 */
public interface ExpectedWaypointsTripRepository extends JooqRepository<ExpectedWaypointsTrip, ExpectedWaypointsTripRecord, UUID> {
    /**
     * Поиск плановых точек маршрута по трипу.
     *
     * @param tripId     идентификатор трипа.
     * @return список координат.
     */
    Optional<ExpectedWaypointsTripRecord> findByTripId(UUID tripId);

    /**
     * Получение списка маршрутов для которых не построен плановый маршрут
     *
     * @return список маршрутов
     */
    List<ExpectedWaypointsTripRecord> getAllNotHandledRecords();

    /**
     * Обработка завершения создания плановых маршрутов (FORMULA, TWO_GIS)
     * @param handledTripRecords список поездок
     */
    void handleCompliteCreateExpectedRoute(List<ExpectedRouteRecord> handledTripRecords);

    /**
     * Обновление количества попыток генерации планового маршрута до максимального для исклюбчения проблемных маршрутов
     * @param expectedRouteWithWrongWaypointsUUIDList список id плановых маршрутов
     */
    void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList);
}

