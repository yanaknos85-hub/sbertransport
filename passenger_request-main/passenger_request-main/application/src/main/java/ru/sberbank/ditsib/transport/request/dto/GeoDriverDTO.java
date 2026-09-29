package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO with driver's coordinates
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Координаты", description = "Данные о координатах водителя")
public class GeoDriverDTO {
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
