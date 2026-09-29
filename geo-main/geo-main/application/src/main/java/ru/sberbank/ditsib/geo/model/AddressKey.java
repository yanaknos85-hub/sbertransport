package ru.sberbank.ditsib.geo.model;

import lombok.*;

import java.io.Serializable;

/**
 * Key of address.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AddressKey implements Serializable {
    
    /**
     * Latitude.
     */
    private Double latitude;
    
    /**
     * Longitude.
     */
    private Double longitude;
    
    /**
     * Provider.
     */
    private String provider;
}
