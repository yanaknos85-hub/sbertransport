package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO с KPI заказа из совместной поездки
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
@Schema(title = "KPI заказа из совместной поездки", description = "Рассчитанные параметры заказа на поездку ")
public class MagentaOrgOrderKpiDTO {
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    @NotBlank
    @Schema(description = "Id заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Positive(message = "Part must be greater than 0")
    @Schema(description = "Доля заказа в общей стоимости", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double costSharePart;
    
    /**
     * рассчитанное время поездки для текущего заказа
     */
    @Positive(message = "Ride time must be greater than 0")
    @Schema(description = "Расчётное время в пути", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rideTimeMin;
    
    /**
     * экономия в рублях для текущего заказа
     */
    @Min(0)
    @Schema(description = "Экономия в рублях для текущего заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double savings;
    
    /**
     * экономия в процентах для текущего заказа
     */
    @Min(0)
    @Schema(description = "Процент экономии", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double savingsPct;
    
    /**
     * Рассчитанный километраж поездки для текущего заказа
     */
    @Positive(message = "Order distance must be greater than 0")
    @Schema(description = "Расчётное расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderDistanceKm;
    
}
