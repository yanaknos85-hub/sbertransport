package ru.sberbank.ditsib.transport.srm.dto.internal;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO точки маршрута для публикации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class SrmSingleRequestDTO {
    
    /**
     * ID заявки
     */
    @Schema(description = "ID заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID requestId;
    
    /**
     * Количество пассажиров
     */
    @Schema(description = "Число пассажиров")
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
     * Оптимизация маршрута.
     */
    @Schema(description = "Режим ЭКСПРЕСС для грузов", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Boolean cargoExpress = Boolean.FALSE;
    
    /**
     * Заявка-кандидат.
     */
    @Schema(description = "Заявка-кандидат", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private boolean candidate = false;
    
    /**
     * точки маршрута
     */
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    @Builder.Default
    @Schema(description = "Точки маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SrmWaypointPostDTO> waypoints = new ArrayList<>();
}
