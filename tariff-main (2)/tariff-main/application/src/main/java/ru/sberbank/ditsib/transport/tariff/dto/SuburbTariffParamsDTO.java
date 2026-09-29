package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Параметры тарифа за чертой города
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Параметры тарифа за чертой города", description = "Параметры тарифа за чертой города")
public class SuburbTariffParamsDTO {
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Цена за км  за чертой города, коп", defaultValue = "0", minimum = "0", maximum = "100000")
    private final Integer costPerKmSuburb = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Цена за минуту  за чертой города, коп", defaultValue = "0", minimum = "0",
            maximum = "100000")
    private final Integer costPerMinSuburb = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость 1 км платной подачи за чертой города, коп.", defaultValue = "0", minimum = "0",
            maximum = "100000")
    private final Integer suburbServiceCostPerKm = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость 1 мин платной подачи за чертой города, коп.", defaultValue = "0", minimum = "0",
            maximum = "100000")
    private final Integer suburbServiceCostPerMin = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость пробега 1 км межрегиональной поездки, коп.", defaultValue = "0", minimum = "0",
            maximum = "100000")
    private final Integer costPerKmInterRegion = 0;
    
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Schema(description = "Стоимость 1 минуты межрегиональной поездки, коп", defaultValue = "0", minimum = "0",
            maximum = "100000")
    private final Integer costPerMinInterRegion = 0;
}
