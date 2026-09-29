package ru.sberbank.ditsib.geo.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Coordinates.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
public class Coordinates {
    
    /**
     * Latitude.
     */
    private double latitude;
    
    /**
     * Longitude.
     */
    private double longitude;
    
}
