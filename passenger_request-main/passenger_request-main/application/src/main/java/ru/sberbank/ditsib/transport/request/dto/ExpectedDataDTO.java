package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object with data about expected trip data.
 */
@Data
@Schema(title = "Расчетные данные по маршруту", description = "Данные о маршруте, полученные в результате расчета " +
                                                              "длины маршрута, времени в пути и стоимости маршрута")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExpectedDataDTO {
    
    /**
     * Expected route segments
     */
    @Size(min = 1, message = "Маршрут должен состоять минимум из 1 отрезка")
    @Schema(description = "Части маршрута между ключевых точек", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private final List<RouteSegmentDTO> segments = new ArrayList<>();
    /**
     * Expected waypoints
     */
    @Size(min = 2, max = 50, message = "Маршрут должен состоять минимум из 2, максимум из 50 точек")
    @Schema(description = "Ключевые точки", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "2", maximum = "50")
    @Builder.Default
    private final List<WaypointDTO> waypoints = new ArrayList<>();
    
    /**
     * cost
     */
    @Min(0)
    @Schema(description = "Стоимость поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private double cost;
    
    /**
     * cost by outcome tariff
     */
    @Min(0)
    @Schema(description = "Стоимость поездки по расходному тарифу", requiredMode = Schema.RequiredMode.REQUIRED)
    private double outcomeCost;
    
    @Min(0)
    @Schema(description = "Бонусы, которые будут списаны со стоимости поездки")
    private Long bonusCost;
    
    /**
     * distance
     */
    @Min(0)
    @Schema(description = "Дальность поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private double distance;
    /**
     * time
     */
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Duration time;
}
