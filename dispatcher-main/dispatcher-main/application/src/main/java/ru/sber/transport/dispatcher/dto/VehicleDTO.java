package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Object with data of Vehicle.
 */
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о транспорте", description = "Данные транспорта")
public class VehicleDTO extends NewVehicleDTO {
    
    /**
     * ID.
     */
    @Schema(description = "Идентификатор")
    private UUID id;

    @Schema(description = "Активность")
    @Builder.Default
    private Boolean active = true;

    @Schema(description = "Идентификатор ЭПЛ")
    private UUID ewbId;
}
