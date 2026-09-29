package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Параметры тарифа по допустимым отклонениям от реестра контрагента", description = "Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента")
public class ContractorDeviationsTariffDTO {
    // Допустимый % отклонения протяженности маршрута от контрагента и расчетной протяженности в АС
    @Min(0)
    @Max(100)
    @Schema(description = "Допустимый % отклонения протяженности маршрута от контрагента и расчетной протяженности в АС",
            minimum = "0", maximum = "100")
    private Integer maxDiffComputedDistancePercent;
    
    // Допустимый % отклонения протяженности маршрута от контрагента и фактической протяженности в АС
    @Min(0)
    @Max(100)
    @Schema(description = "Допустимый % отклонения протяженности маршрута от контрагента и фактической протяженности в АС",
            minimum = "0", maximum = "100")
    private Integer maxDiffFactDistancePercent;
    
    // Допустимый % отклонения стоимости поездки в реестре контрагента и расчетной стоимости в АС
    @Min(0)
    @Max(100)
    @Schema(description = "Допустимый % отклонения стоимости поездки в реестре контрагента и расчетной стоимости в АС",
            minimum = "0", maximum = "100")
    @Nullable
    private Integer maxDiffComputedCostPercent;
    
    // Допустимый % отклонения стоимости поездки в реестре контрагента и возвращенной стоимости от подрядчика
    @Min(0)
    @Max(100)
    @Schema(description = "Допустимый % отклонения стоимости поездки в реестре контрагента и возвращенной стоимости от подрядчика",
            minimum = "0", maximum = "100")
    private Integer maxDiffContractorCostPercent;
    
    // Допустимый % отклонения времени ожидания в реестре контрагента и времени ожидания предварительно рассчитанного
    // в АС.
    @Min(0)
    @Max(100)
    @Schema(description = "Допустимый % отклонения времени ожидания в реестре контрагента и времени ожидания " +
                          "предварительно рассчитанного в АС", minimum = "0", maximum = "100")
    private Integer maxDiffComputedWaitingPercent;
}
