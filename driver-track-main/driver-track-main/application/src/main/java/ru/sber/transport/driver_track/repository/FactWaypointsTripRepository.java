package ru.sber.transport.driver_track.repository;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.driver_track.database.driver_track.tables.FactWaypointsTrip;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с фактическими точками маршрута.
 */
public interface FactWaypointsTripRepository extends JooqRepository<FactWaypointsTrip, FactWaypointsTripRecord, UUID> {
    /**
     * Поиск фактических точек маршрута по трипу.
     *
     * @param tripId     идентификатор трипа.
     * @return список координат.
     */
    Optional<FactWaypointsTripRecord> findByTripId(UUID tripId);

    /**
     * Получение списка маршрутов для которых не построен фактический маршрут TWO_GIS
     *
     * @return список маршрутов
     */
    List<FactWaypointsTripRecord> getAllNotHandledRecords();

    /**
     * Обработка завершения создания фактических маршрутов (FORMULA, TWO_GIS)
     * @param handledTripRecords список поездок
     */
    void handleCompliteCreateExpectedRoute(List<RouteRecord> handledTripRecords);

    /**
     * Обновление количества попыток генерации планового маршрута до максимального для исклюбчения проблемных маршрутов
     * @param expectedRouteWithWrongWaypointsUUIDList список id плановых маршрутов
     */
    void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList);
}

