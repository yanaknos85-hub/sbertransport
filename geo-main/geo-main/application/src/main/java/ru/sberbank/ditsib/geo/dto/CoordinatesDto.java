package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Coordinates.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Schema(title = "Координаты", description = "Данные о координатах")
public class CoordinatesDto {
    
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
