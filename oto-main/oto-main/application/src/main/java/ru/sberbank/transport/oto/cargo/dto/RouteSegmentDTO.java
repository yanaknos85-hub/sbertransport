package ru.sberbank.transport.oto.cargo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object with data about route segment
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Перегон", description = "Участок маршрута между ключевых точек")
public class RouteSegmentDTO {
    

    @Min(0)
    @Schema(description = "Стоимость")
    private double cost;

    @Min(0)
    @Schema(description = "Дальность")
    private double distance;

    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время")
    private Duration time;

    @Size(min = 2)
    @Builder.Default
    @Schema(description = "Маршрутные точки")
    private final List<CoordinatesDTO> coordinates = new ArrayList<>();
}
