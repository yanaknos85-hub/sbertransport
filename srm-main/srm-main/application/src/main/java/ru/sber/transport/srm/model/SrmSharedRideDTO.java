package ru.sber.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sber.transport.constants.PointMatchingType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO совместной поездки magenta
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class SrmSharedRideDTO {
    
    /**
     * ID созданной поездки
     */
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New ride id cannot be blank")
    private UUID id;
    
    /**
     * Айди старого типа
     */
    @Schema(description = "Айди старого типа")
    private Integer oldId;
    
    /**
     * Идентификатор тарифа
     */
    @Schema(description = "Идентификатор тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "tariffId cannot be null")
    private UUID tariffId;
    
    /**
     * Временная зона.
     */
    @Schema(description = "Временная зона", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeZone;
    
    /**
     * Тип транспорта
     */
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "transportType cannot be null")
    private TransportTypeEnum transportType;
    
    /**
     * Тип совмещения точек.
     */
    @Schema(description = "Тип совмещения точек", requiredMode = Schema.RequiredMode.REQUIRED)
    private PointMatchingType pointMatchingType;
    
    /**
     * Расчитанная полная стоимость, руб
     */
    @Schema(description = "Расчитанная полная стоимость", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rideCost;
    
    /**
     * Расчитаное полное расстояние, км
     */
    @Schema(description = "Расчитаное полное расстояние", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double rideDistance;
    
    /**
     * Расчитаное полное время поездки, сек
     */
    @Schema(description = "Расчитаное полное время поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rideTime;
    
    /**
     * Остановки
     */
    @Schema(description = "Остановки маршртута", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    @Builder.Default
    private List<SrmWaypointGetDTO> waypoints = new ArrayList<>();
    
    /**
     * Остановки
     */
    @Schema(description = "Остановки маршртута оптимизированыые", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    @Builder.Default
    private List<SrmWaypointFinalDTO> waypointsFinal = new ArrayList<>();
    
    /**
     * KPI по отдельны заказам в поездке
     */
    @Schema(description = "KPI по отдельны заказам в поездке", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private List<SrmRequestKpiDTO> requestKpiList = new ArrayList<>();
    
    /**
     * Флаг активности поездки. false -  удалена/отменена
     */
    @Schema(description = "Флаг активности поездки", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private boolean active = true;
}
