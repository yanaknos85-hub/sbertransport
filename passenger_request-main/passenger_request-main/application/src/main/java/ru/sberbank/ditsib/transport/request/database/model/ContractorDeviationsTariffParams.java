package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента
 **/
@ToString
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractorDeviationsTariffParams {
    
    /**
     * Допустимый % отклонения протяженности маршрута от контрагента и расчетной протяженности в АС.
     */
    @Min(0)
    @Max(100)
    @Column(name = "contractor_max_diff_computed_distance_percent")
    private Integer maxDiffComputedDistancePercent;
    
    /**
     * Допустимый % отклонения протяженности маршрута от контрагента и фактической протяженности в АС.
     */
    @Min(0)
    @Max(100)
    @Column(name = "contractor_max_diff_fact_distance_percent")
    private Integer maxDiffFactDistancePercent;
    
    /**
     * Допустимый % отклонения стоимости поездки в реестре контрагента и расчетной стоимости в АС.
     */
    @Min(0)
    @Max(100)
    @Column(name = "contractor_max_diff_computed_cost_percent")
    private Integer maxDiffComputedCostPercent;
    
    /**
     * Допустимый % отклонения стоимости поездки в реестре контрагента и возвращенной стоимости от подрядчика.
     */
    @Min(0)
    @Max(100)
    @Column(name = "contractor_max_diff_contractor_cost_percent")
    private Integer maxDiffContractorCostPercent;
    
    /**
     * Допустимый % отклонения времени ожидания в реестре контрагента и времени ожидания предварительно
     * рассчитанного в АС.
     */
    @Min(0)
    @Max(100)
    @Column(name = "contractor_max_diff_computed_waiting_percent")
    private Integer maxDiffComputedWaitingPercent;
    
}
