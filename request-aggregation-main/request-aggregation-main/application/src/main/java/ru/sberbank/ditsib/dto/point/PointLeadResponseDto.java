package ru.sberbank.ditsib.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.enumerate.PointType;

import java.util.UUID;

@Schema(name = "PointLeadResponseDto", description = "Ответ о созданной точке маршрута")
public record PointLeadResponseDto(

        @Schema(description = "Идентификатор точки маршрута")
        UUID id,
        @Schema(description = "Порядковый номер точки маршрута")
        int pointNumber,
        @Schema(description = "Тип точки маршрута", example = "START")
        PointType typePoint,
        @Schema(description = "Координаты точки маршрута: долгота")
        double longitude,
        @Schema(description = "Координаты точки маршрута: широта")
        double latitude,
        @Schema(description = "Адрес точки маршрута")
        String waypoint
) {
}
