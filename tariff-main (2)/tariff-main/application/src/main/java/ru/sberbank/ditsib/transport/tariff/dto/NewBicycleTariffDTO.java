package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO с данными по публикуемому тарифу велосипеда
 */
@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по новому тарифу велосипеда", description = "Данные по тарифу")
public class NewBicycleTariffDTO extends NewBaseTariffDto {
    
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Schema(description = "Цена за КМ", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerKm;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость за минуту пути, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer rideCostPerMin;
    
    @Min(0)
    @Max(1000_00)
    @NotNull
    @Schema(description = "Стоимость брони FIX, коп", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private Integer bookingCost;
    
    
    @Builder.Default
    @Schema(description = "Набор коэффициентов по времени")
    private final TimedTariffParamsDTO timedTariffParams = new TimedTariffParamsDTO();
    
    @Min(0)
    @Max(10)
    @Builder.Default
    @Schema(description = "Коэффициент на страхование", defaultValue = "1", minimum = "0")
    private final Double coefInsurance = 1d;
}
