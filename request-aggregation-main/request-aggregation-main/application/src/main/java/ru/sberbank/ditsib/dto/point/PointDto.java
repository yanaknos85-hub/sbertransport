package ru.sberbank.ditsib.dto.point;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.sberbank.ditsib.enumerate.PointType;
import ru.sberbank.ditsib.enumerate.TransportType;

import java.util.UUID;

/**
 * Точка маршрута
 */
public record PointDto(
        @NotNull
        PointType type,
        double latitude,
        double longitude,
        @Positive
        int pointNumber,
        int cost,
        @NotNull
        TransportType transportType,
        UUID mainLeadId
) {
}
