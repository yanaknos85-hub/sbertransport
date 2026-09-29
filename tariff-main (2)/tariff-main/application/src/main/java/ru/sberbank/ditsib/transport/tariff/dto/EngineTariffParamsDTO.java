package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

/**
 * Дополнительные параметры тарифа, используемые для определения коэффициента за объем двигателя личного ТС
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Коэффициенты дивгателя личного авто",
        description = "Дополнительные параметры тарифа, используемые для определения коэффициента за объем двигателя личного ТС")
public class EngineTariffParamsDTO {
    
    @Positive
    @Max(10)
    @Builder.Default
    @Schema(description = "К объема двигателя для ТС до 1,6 л. ( только для бензиновых двигателей)",
            defaultValue = "1", minimum = "0", exclusiveMinimum = true, maximum = "10")
    private final Double coefEngine1_6 = 1d;
    
    @Positive
    @Max(10)
    @Builder.Default
    @Schema(description = "К объема двигателя для ТС от 1,6 до 2,0 л (только для бензиновых двигателей)",
            defaultValue = "1", minimum = "0", exclusiveMinimum = true, maximum = "10")
    private final Double coefEngine1_6_to_2_0 = 1d;
    
    @Positive
    @Max(10)
    @Builder.Default
    @Schema(description = "К объема двигателя для ТС от 2,0 до 2,5 л (только для бензиновых двигателей)",
            defaultValue = "1", minimum = "0", exclusiveMinimum = true, maximum = "10")
    private final Double coefEngine2_0_to_2_5 = 1d;
}
