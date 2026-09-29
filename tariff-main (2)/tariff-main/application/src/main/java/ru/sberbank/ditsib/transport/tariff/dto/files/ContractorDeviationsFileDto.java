package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента
 **/
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContractorDeviationsFileDto {
    // Допустимый % отклонения протяженности маршрута контрагента от расчетной протяженности в АС
    @Builder.Default
    @NotBlank
    private Integer maxDiffComputedDistancePercent = 0; // AJ
    
    // Допустимый % отклонения протяженности маршрута контрагента от фактической протяженности в АС
    @Builder.Default
    @NotBlank
    private Integer maxDiffFactDistancePercent = 0; // AJ
    
    // Допустимый % отклонения стоимости поездки в реестре контрагента и расчетной стоимости в АС
    @Builder.Default
    @NotBlank
    private Integer maxDiffComputedCostPercent = 0; // AI
    
    // Допустимый % отклонения стоимости поездки в реестре контрагента и возвращенной стоимости от подрядчика
    @Builder.Default
    @NotBlank
    private Integer maxDiffContractorCostPercent = 0; // AI
    
    // Допустимый % отклонения времени ожидания в реестре контрагента и времени ожидания предварительно
    // рассчитанного в АС.
    @Builder.Default
    @NotBlank
    private Integer maxDiffComputedWaitingPercent = 0; // AK
    
}
