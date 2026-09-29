package ru.sber.transport.tariff.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Параметры пробега за чертой города
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Параметры пробега за чертой города", description = "Параметры пробега за чертой города")
public class SuburbTripDataDTO {
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Пробег поездки за чертой города в км", defaultValue = "0", minimum = "0")
    private final double suburbDistance = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Пробег платной подачи за чертой города в км", defaultValue = "0", minimum = "0")
    private final double suburbServiceDistance = 0d;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Продолжительность платной подачи за чертой города, мин", defaultValue = "0", minimum = "0")
    private final int suburbServiceTime = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Продолжительность поездки за чертой города, мин", defaultValue = "0", minimum = "0")
    private final int suburbTime = 0;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Пробег межрегиональной поездки, км", defaultValue = "0", minimum = "0")
    private final double interRegionDistance = 0d;
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Продолжительность межрегиональной поездки, мин", defaultValue = "0", minimum = "0")
    private final int interRegionTime = 0;
}