package ru.sberbank.ditsib.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.enumerate.PointType;

@Schema(name = "PointLeadRequestDto", description = "Запрос на создание точки маршрута заявки")
public record PointLeadRequestDto(
        @Schema(description = "Тип точки маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PointType typePoint,

        @Schema(description = "Долгота", requiredMode = Schema.RequiredMode.REQUIRED)
        double longitude,

        @Schema(description = "Широта", requiredMode = Schema.RequiredMode.REQUIRED)
        double latitude,

        @Schema(description = "Адрес точки маршрута", requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @Size(max = 255)
        @NotBlank
        String waypoint
) {
}
