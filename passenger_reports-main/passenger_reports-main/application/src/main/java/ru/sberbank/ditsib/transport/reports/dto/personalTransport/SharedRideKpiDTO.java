package ru.sberbank.ditsib.transport.reports.dto.personalTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(title = "Расчетный KPI для совместной поездки", description = "Расчетный KPI для совместной поездки")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SharedRideKpiDTO {

    @Schema(description = "Полная стоимость")
    private Double totalCost;
    
    @Schema(description = "Полное расстояние")
    private Double totalDistanceKm;
    
    @Schema(description = "Рассчитанное время поездки")
    private Integer totalTimeMin;
}
