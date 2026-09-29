package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект с данными об отклонениях.
 */
@Getter
@Setter
public class CoopDeviationFileDto {
    
    /**
     * Экономия поездки.
     */
    private double savingsDeviationPct;
    
    /**
     * Максимальное отклонение по расстоянию.
     */
    private double distanceDeviationKm;
    
    /**
     * Максимальное отклонение по времени.
     */
    private long timeDeviationMin;
    
    /**
     * Триггерное время.
     */
    private long minCancelTimeMin;
}
