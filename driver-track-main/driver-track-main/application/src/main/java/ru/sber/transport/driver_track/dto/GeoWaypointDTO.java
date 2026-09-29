package ru.sber.transport.driver_track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(title = "Данные точки", description = "Координаты")
public class GeoWaypointDTO {

    @NotNull
    @Schema(description = "Широта")
    private double latitude;

    @NotNull
    @Schema(description = "Долгота")
    private double longitude;
}
