package ru.sber.transport.request.messaging;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Информация о поездке - точки маршрута, пассажиры
 *
 * @param countPassengers Количество пассажиров
 * @param planStart       Планируемое время начала поездки
 * @param startAddress    Адрес точки отправления по маршруту
 * @param endAddress      Адрес точки назначения маршрута
 * @param passengers      Список пассажиров
 * @param initiator       Инициатор поездки
 * @param waypoints       Список точек маршрута
 */
public record RequestInformationMessage(
        Integer countPassengers,
        LocalDateTime planStart,
        String startAddress,
        String endAddress,
        @NotEmpty
        List<PassengerMessage> passengers,
        @NotNull
        PassengerMessage initiator,
        List<OutContractorWaypointMessage> waypoints
) {
}
