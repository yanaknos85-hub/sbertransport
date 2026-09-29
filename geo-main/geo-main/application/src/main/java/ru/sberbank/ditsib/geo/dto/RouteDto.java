package ru.sberbank.ditsib.geo.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.DurationMillisConverter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Object with data with requested route.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Schema(title = "Маршрут", description = "Данные о маршруте")
public class RouteDto {
    
    /**
     * List of maneuvers.
     */
    @Builder.Default
    @Schema(description = "Точки маршрута")
    private final List<WaypointDto> waypoints = new ArrayList<>();
    /**
     * Distance.
     */
    @Schema(description = "Расстояние")
    private Double distance;
    /**
     * Time.
     */
    @JsonSerialize(using = DurationMillisConverter.class)
    @Schema(description = "Время")
    private Duration time;
    /**
     * Coordinates of route.
     */
    @Builder.Default
    @Schema(description = "Части маршрута")
    private List<RoutePartDto> segments = new ArrayList<>();

    public static RouteDto empty() {
        return RouteDto.builder()
                .distance(0.0)
                .time(Duration.ZERO)
                .build();
    }
}
