package ru.sberbank.ditsib.transport.reports.dto;

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
 * Data transfer object with data about expected trip data.
 */
@Data
@Schema(title = "Расчетные данные по маршруту", description = "Данные о маршруте, полученные в результате расчета " +
        "длины маршрута, времени в пути и стоимости маршрута")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExpectedDataDTO {

    @Size(min = 1, message = "Маршрут должен состоять минимум из 1 отрезка")
    @Schema(description = "Части маршрута между ключевых точек")
    @Builder.Default
    private final List<RouteSegmentDTO> segments = new ArrayList<>();

    @Size(min = 2, max = 50, message = "Маршрут должен состоять минимум из 2, максимум из 50 точек")
    @Schema(description = "Ключевые точки", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "2", maximum = "50")
    @Builder.Default
    private final List<WaypointDTO> waypoints = new ArrayList<>();

    @Schema(description = "Общее кол-во пунктов маршрута")
    private Integer waypointsCount;

    @Schema(description = "Кол-во пунктов маршрута поездки с совпадением координат 'Отметиться'")
    private Integer waypointsCountWithCheckIn;

    @Schema(description = "Кол-во пунктов маршрута поездки без совпадения координат 'Отметиться'")
    private Integer waypointsCountWithoutCheckIn;

    @Min(0)
    @Schema(description = "Стоимость поездки")
    private double cost;

    @Min(0)
    @Schema(description = "Дальность поездки")
    private double distance;
    
    @Schema(description = "Время поездки")
    private long time;

    @Schema(description = "Предполагаемое время в пути")
    private Duration expectedTime;
}
