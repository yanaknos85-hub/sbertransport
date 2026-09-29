package ru.sber.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

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
public class SrmRequestKpiDTO {
    
    /**
     * ID заказа
     */
    @NotBlank
    @Schema(description = "Id заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    /**
     * ID заказа типа мадженты
     */
    @NotBlank
    @Schema(description = "Id заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer oldId;
    
    /**
     * Время начала поездки
     */
    @Schema(description = "Время начала поездки")
    private ZonedDateTime pickupTime;
    
    /**
     * Время завершения поездки
     */
    @Schema(description = "Время завершения поездки")
    private ZonedDateTime dropTime;
    
    /**
     * Количество пассажиров
     */
    @Schema(description = "Количество пассажиров")
    private Integer requiredPassengers;
    
    /**
     * Объем груза
     */
    @Schema(description = "Объем груза")
    private Double requiredVolume;
    
    /**
     * Вес груза
     */
    @Schema(description = "Вес груза")
    private Double requiredWeight;
    
    /**
     * Рассчитанный километраж поездки для текущего заказа
     */
    @Positive(message = "Order distance must be greater than 0")
    @Schema(description = "Расчётное расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double requestDistance;
    
    /**
     * Рассчитанное время поездки для текущего заказа в секундах
     */
    @Positive(message = "Ride time must be greater than 0")
    @Schema(description = "Расчётное время в пути", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestTime;
    
    /**
     * Рассчитанная цена поездки по тарифу в копейках
     */
    @Positive(message = "Part must be greater than 0")
    @Schema(description = "Рассчитанная цена поездки по тарифу в копейках", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestPrice;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Positive(message = "Part must be greater than 0")
    @Schema(description = "Доля заказа в общей стоимости", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double costSharePart;
    
    /**
     * Экономия в рублях для текущего заказа
     */
    @Min(0)
    @Schema(description = "Экономия в рублях для текущего заказа", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long savingsCash;
    
    /**
     * Экономия в процентах для текущего заказа
     */
    @Min(0)
    @Schema(description = "Процент экономии", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double savingsProcents;
    
    /**
     * Время создания.
     */
    @Schema(description = "Время создания")
    private LocalDateTime creationTime;
    
    /**
     * Порядковый номер.
     */
    @NotNull
    @Schema(description = "Порядковый номер")
    private Integer orderingIndex;
    
    /**
     * Режим ЭКСПРЕСС для грузов.
     */
    @Schema(description = "Режим ЭКСПРЕСС для грузов")
    @Builder.Default
    private Boolean cargoExpress = Boolean.FALSE;
    
    /**
     * Заявка-кандидат.
     */
    @Schema(description = "Заявка-кандидат", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean candidate;
}
