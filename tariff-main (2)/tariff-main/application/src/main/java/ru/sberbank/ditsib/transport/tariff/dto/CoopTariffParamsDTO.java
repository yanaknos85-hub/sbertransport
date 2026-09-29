package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Дополнительные параметры тарифа, используемые для подбора совместных поездок
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Параметры тарифа за чертой города", description = "Параметры тарифа за чертой города")
public class CoopTariffParamsDTO {
    
    //Предельно-допустимое отклонение по минимальной экономии
    @Min(0)
    @Max(100)
    @Builder.Default
    @Schema(description = "Предельно-допустимое отклонение по минимальной экономии, процентов",
            defaultValue = "0", minimum = "0", maximum = "100")
    private final Double savingsDeviationPct = 0d;
    
    //Предельно-допустимое отклонение по километражу
    @Min(0)
    @Max(2000)
    @Builder.Default
    @Schema(description = "Предельно-допустимое отклонение по километражу, 0 - отклонение запрещено",
            defaultValue = "0", minimum = "0", maximum = "2000")
    private final Double distanceDeviationKm = 0d;
    
    //Предельно-допустимое отклонение по времени в минутах
    @Min(0)
    @Max(43_200)
    @Builder.Default
    @Schema(description = "Отклонение по времени прибытия во вторую точку в минутах, 0 - отклонение запрещено",
            required = false, defaultValue = "0", minimum = "0", maximum = "43200")
    private final Integer timeDeviationMin = 0;
    
    //Триггерное время (За какое время до поездки ее можно отменить)
    @Min(0)
    @Max(60)
    @Builder.Default
    @Schema(description = "Триггерное время (За какое время до поездки ее можно отменить)", defaultValue = "30",
            required = false, minimum = "0", maximum = "60")
    private final Integer minCancelTimeMin = 30;
    
}
