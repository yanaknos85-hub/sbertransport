package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import java.time.Duration;

/**
 * Расчетные данные по маршруту
 */
@Getter
@Builder
@Schema(
        title = "Расчетные данные по маршруту",
        description = "Данные о маршруте, полученные в результате расчета длины маршрута, " +
                      "времени в пути и стоимости маршрута")
@NoArgsConstructor
@AllArgsConstructor
public class PublicExpectedDataDTO {
    
    /**
     * Стоимость поездки
     */
    @NotNull @Min(0)
    @Schema(description = "Стоимость поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private double cost;
    /**
     * Дальность поездки
     */
    @NotNull @Min(0)
    @Schema(description = "Дальность поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private double distance;
    
    /**
     * Время поездки
     */
    @NotNull
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Duration time;
}
