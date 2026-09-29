package ru.sberbank.ditsib.geo.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import ru.sberbank.ditsib.converters.DurationMillisConverter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data of points.
 */
@Getter
@Builder
@Schema(title = "Часть маршрута", description = "Данные о части маршрута")
public class RoutePartDto {
    
    /**
     * Distance.
     */
    @Schema(description = "Расстояние")
    private final Double distance;
    
    /**
     * Time.
     */
    @JsonSerialize(using = DurationMillisConverter.class)
    @Schema(description = "Время")
    private final Duration time;
    
    /**
     * List of maneuvers.
     */
    @Builder.Default
    @Schema(description = "Координаты")
    private final List<CoordinatesDto> coordinates = new ArrayList<>();
    
}
