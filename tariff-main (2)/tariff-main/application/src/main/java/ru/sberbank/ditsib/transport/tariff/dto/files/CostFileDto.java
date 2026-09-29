package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект данных цен.
 */
@Getter
@Setter
public class CostFileDto {
    
    /**
     * Бронь/подача.
     */
    private double book;
    
    /**
     * Подача за км.
     */
    private double submissionDistance;
    
    /**
     * Подача за мин.
     */
    private double submissionTime;
    
    /**
     * За время.
     */
    private double time;
    
    /**
     * За расстояние.
     */
    private double distance;
    
    /**
     * За расстояние (между регионами).
     */
    private double distanceInterregional;
    
    /**
     * За ожидание.
     */
    private double waitTime;
    
    /**
     * За ожидание в промежуточной точке.
     */
    private double waitIntermediateTime;
    
    /**
     * Минимальная поездка по расстоянию.
     */
    private double minRideDistance;
    
    /**
     * Минимальная поездка по времени.
     */
    private double minRideTime;
    
    /**
     * Поездка за городом.
     */
    private CostFileDto suburb;
    
    /**
     * Межрегиональная.
     */
    private CostFileDto interregional;
    
}
