package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

/**
 * Object of data about tariffs.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Коэффициенты тарифа по времени суток, дням недели", description = "Данные по временным коэффициентам" +
                                                                                   " тарифа")
public class TimedTariffParamsDTO {
    @Schema(description = "Коэффициент временного интервала поездки: утро будние дни 07:00-10:00", minimum = "0",
            exclusiveMinimum = true, maximum = "10", defaultValue = "1")
    @Positive
    @Max(10)
    @Builder.Default
    private final Double coefWorkDayMorning = 1d;
    
    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 10:00-18:00", minimum = "0",
            exclusiveMinimum = true, maximum = "10", defaultValue = "1")
    @Positive
    @Max(10)
    @Builder.Default
    private final Double coefWorkDayNoon = 1d;
    
    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 18:00-22:00", minimum = "0",
            exclusiveMinimum = true, maximum = "10", defaultValue = "1")
    @Positive
    @Max(10)
    @Builder.Default
    private final Double coefWorkDayEvening = 1d;
    
    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 22:00-07:00", minimum = "0",
            exclusiveMinimum = true, maximum = "10", defaultValue = "1")
    @Positive
    @Max(10)
    @Builder.Default
    private final Double coefWorkDayNight = 1d;
    
    @Schema(description = "Коэффициент выходного дня: СБ, ВСКР", minimum = "0",
            exclusiveMinimum = true, maximum = "10", defaultValue = "1")
    @Positive
    @Max(10)
    @Builder.Default
    private final Double coefDayOff = 1d;
}
