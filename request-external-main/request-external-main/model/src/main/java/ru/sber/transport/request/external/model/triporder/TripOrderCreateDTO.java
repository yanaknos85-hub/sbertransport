package ru.sber.transport.request.external.model.triporder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;

/**
 * DTO для создания заказа на поездку
 *
 * @param date Дата поездки
 * @param waypoints Путевые точки поездки
 * @param purposeId Цель поездки
 * @param tariff Тариф поездки
 * @param comment Комментарий к поездке
 * @param taxiCost Стоимость(в рубля) поездки на корпоративном такси
 */
public record TripOrderCreateDTO(
        OffsetDateTime date,
        List<WaypointDTO> waypoints,
        UUID purposeId,
        Tariff tariff,
        String comment,
        BigDecimal taxiCost
) {
}
