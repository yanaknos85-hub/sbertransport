package ru.sberbank.ditsib.transport.request.database.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity of single geo coordinate pair
 */

@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Coordinates {
    
    /**
     * Latitude
     */
    @Column(nullable = false)
    private double latitude;
    
    /**
     * Longitude
     */
    @Column(nullable = false)
    private double longitude;
}
