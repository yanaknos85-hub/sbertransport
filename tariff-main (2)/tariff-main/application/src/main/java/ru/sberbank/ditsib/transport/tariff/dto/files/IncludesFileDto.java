package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект с данными о том, что включено в тариф.
 */
@Getter
@Setter
public class IncludesFileDto {
    
    /**
     * Расстояние.
     */
    private double distance;
    
    /**
     * Время.
     */
    private long time;
    
    /**
     * Время ожидания.
     */
    private long waitingTime;
    
    /**
     * Минимальное расстояние.
     */
    private long minimumDistance;
    
    /**
     * Минимальное время.
     */
    private long minimumTime;
    
    /**
     * Минимальное время отмены заказа.
     */
    private long minimumCancelTime;
    
}
