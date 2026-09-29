package ru.sber.transport.driver_track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(title = "Сегмент пути", description = "Сегмент пути")
public class SegmentDTO {

    @Schema(description = "Длина пути")
    private Double distance;

    @Schema(description = "Время пути")
    private Duration time;

    @Schema(description = "Точки пути")
    private List<GeoWaypointDTO> points;
}
