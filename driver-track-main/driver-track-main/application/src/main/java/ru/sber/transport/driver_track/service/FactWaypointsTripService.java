package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с фактическими данными поездки
 */
public interface FactWaypointsTripService {

    /**
     * Получение всех поездок без фактического маршрута
     * @return список поездок
     */
    List<FactWaypointsTripRecord> getAllNotHandledRecords();

    /**
     * Обработка завершения создания фактических маршрутов (FORMULA, TWO_GIS)
     * @param handledTripRecords список поездок
     */
    void handleCompliteCreateExpectedRoute(List<RouteRecord> handledTripRecords);

    /**
     * Сохранение точек фактического маршрута
     * @param factWaypointsTrip  точки маршрута
     */
    void saveFactWaypointsTrip(FactWaypointsTripRecord factWaypointsTrip);

    /**
     * Обновление количества попыток генерации планового маршрута до максимального для исклюбчения проблемных маршрутов
     * @param expectedRouteWithWrongWaypointsUUIDList список id плановых маршрутов
     */
    void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList);
}
