package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * В городе.
 */
@Getter
@Setter
public class UrbanFileDto {
    /**
     * За 1 км, руб.
     */
    private double costPerKm;
    
    /**
     * За 1 мин, руб.
     */
    private double costPerMin;
}
