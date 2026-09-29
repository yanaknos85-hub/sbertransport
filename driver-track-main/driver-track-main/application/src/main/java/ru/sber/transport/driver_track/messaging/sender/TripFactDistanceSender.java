package ru.sber.transport.driver_track.messaging.sender;

import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;

import java.util.Map;
import java.util.UUID;

/**
 * Отправитель фактической дистанции.
 */
public interface TripFactDistanceSender {

    /**
     * Отправка фактической дистанции.
     *
     * @param routes   маршруты.
     * @param tripId   идентификатор трипиа.
     */
    void send(Map<RouteSource, RouteDTO> routes, UUID tripId);
}
