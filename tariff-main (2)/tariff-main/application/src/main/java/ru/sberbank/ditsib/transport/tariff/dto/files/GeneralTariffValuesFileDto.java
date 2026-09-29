package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Общие значения тарифа.
 */
@Getter
@Setter
public class GeneralTariffValuesFileDto {
    /**
     * Стоимость 1 км поездки, руб.
     */
    private double rideCostPerKm;
    
    /**
     * Стоимость 1 мин поездки, руб.
     */
    private double rideCostPerMin;
    
    /**
     * Ожидание в промеж. точке, за 1 мин.
     */
    private double waitIntermediateTime;
}
