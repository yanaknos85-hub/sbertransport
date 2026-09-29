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
@Schema(title = "Маршрут", description = "Маршрут")
public class RouteDTO {

    @Schema(description = "Длина пути")
    private Double distance;

    @Schema(description = "Время пути")
    private Duration time;

    @Schema(description = "Фактические сегменты пути")
    private List<SegmentDTO> segments;
}
