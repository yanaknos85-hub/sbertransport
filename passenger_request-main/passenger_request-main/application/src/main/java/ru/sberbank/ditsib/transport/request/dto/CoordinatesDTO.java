package ru.sberbank.ditsib.transport.request.dto;

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
@EqualsAndHashCode
@Schema(title = "Координаты", description = "Данные о координатах")
public class CoordinatesDTO {
    
    /**
     * Latitude.
     */
    @Schema(description = "Широта")
    private double latitude;
    
    /**
     * Longitude.
     */
    @Schema(description = "Долгота")
    private double longitude;
}
