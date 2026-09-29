package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * За городом.
 */
@Getter
@Setter
public class SuburbanFileDto {
    /**
     * За 1 км, руб.
     */
    private double costPerKm;
    
    /**
     * За 1 мин, руб.
     */
    private double costPerMin;
}
