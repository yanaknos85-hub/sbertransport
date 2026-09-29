package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(title = "Данные точки", description = "Координаты с привязкой ко времени")
public class GeoWaypointDTO {

    @Schema(description = "Широта")
    private double latitude;

    @Schema(description = "Долгота")
    private double longitude;

    @Schema(description = "Временная зона")
    private String timeZone;

    @Schema(description = "ID текущей поездки")
    private UUID currentTripId;

    @Schema(description = "Азимут автомобиля")
    private double azimuth;
}
