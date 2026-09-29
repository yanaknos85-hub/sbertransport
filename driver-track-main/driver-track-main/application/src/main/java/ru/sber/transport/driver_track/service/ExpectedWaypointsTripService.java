package ru.sber.transport.driver_track.service;

import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;
import ru.sber.transport.trip.message.TripMessage;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с плановыми данными поездки
 */
public interface ExpectedWaypointsTripService {
    /**
     * Сохранение плановых точек поездки.
     *
     * @param message сообщение о поездке.
     */
    void save(TripMessage message);

    /**
     * Получение всех поездок без планового маршрута
     * @return список поездок
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
