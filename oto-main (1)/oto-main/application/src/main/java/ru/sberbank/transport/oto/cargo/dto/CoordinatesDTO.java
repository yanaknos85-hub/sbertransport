package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO with single point coordinates
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Координаты", description = "Данные о координатах")
public class CoordinatesDTO {
    
    @Schema(description = "Широта")
    private double latitude;

    @Schema(description = "Долгота")
    private double longitude;
}
