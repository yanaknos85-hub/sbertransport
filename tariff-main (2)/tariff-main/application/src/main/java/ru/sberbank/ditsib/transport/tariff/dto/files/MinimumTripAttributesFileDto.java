package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Атрибуты для минимальной поездки/подачи ТС.
 */
@Getter
@Setter
public class MinimumTripAttributesFileDto {
    /**
     * Минимальное расстояние (км).
     */
    private double minKm;
    
    /**
     * Стоимость минимального расстояния.
     */
    private double minRideCost;
    
    /**
     * Бесплатное время ожидания (мин).
     */
    private long freeWaitingTime;
}
