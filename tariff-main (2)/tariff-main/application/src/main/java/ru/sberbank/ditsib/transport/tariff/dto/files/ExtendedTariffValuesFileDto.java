package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Расширенные значения тарифа.
 */
@Getter
@Setter
public class ExtendedTariffValuesFileDto {
    /**
     * В городе.
     */
    private UrbanFileDto urb = new UrbanFileDto();
    
    /**
     * За городом.
     */
    private SuburbanFileDto suburb = new SuburbanFileDto();
    
    /**
     * Цена за 1 мин. ожидания, руб.
     */
    private long waitCostPerMin;
}
