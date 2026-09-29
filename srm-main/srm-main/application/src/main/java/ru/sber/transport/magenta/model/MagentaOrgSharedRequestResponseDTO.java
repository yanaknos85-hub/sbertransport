package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO совместной поездки magenta
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MagentaOrgSharedRequestResponseDTO {
    
    /**
     * ID созданной поездки
     */
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New ride id cannot be blank")
    private String id;
    
    /**
     * Количество пассажиров
     */
    @Schema(description = "Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Passenger count cannot be null")
    private Integer passengers;
    
    /**
     * ID созданного заказа на поездку
     */
    @Schema(description = "Список идентификаторов новых заказов", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> newOrdersIds = new ArrayList<>();
    
    /**
     * Остановки
     */
    @Schema(description = "Остановки маршртута", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    private List<MagentaOrgWaypointResponseDTO> stops = new ArrayList<>();
    
    /**
     * KPI поездки
     */
    @Schema(description = "Расчётный KPI  поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private MagentaOrgKpiDTO kpi;
}
