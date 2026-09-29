package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO с KPI совместной поездки
 */
@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Schema(title = "KPI совместной поездки", description = "Рассчитанные параметры экономии")
public class MagentaOrgKpiDTO {
    
    /**
     * Расчитанная полная стоимость, руб
     */
    @Schema(description = "Расчитанная полная стоимость", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double totalCost;
    
    /**
     * Расчитаное полное расстояние, км
     */
    @Schema(description = "Расчитаное полное расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double totalDistanceKm;
    
    /**
     * Расчитаное полное время поездки, мин
     */
    @Schema(description = "Расчитаное полное время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalTimeMin;
    
    /**
     * KPI по отдельны заказам в поездке
     */
    @Schema(description = "KPI по отдельны заказам в поездке", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MagentaOrgOrderKpiDTO> ordersKpi;
}
