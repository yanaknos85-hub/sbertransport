package ru.sberbank.ditsib.dto;

import java.math.BigDecimal;

/**
 * Информация о маршруте между двумя точками.
 */
public record RouteInfo(

        BigDecimal distance,
        Long timeInSeconds,
        String distanceUnit
) {
}
