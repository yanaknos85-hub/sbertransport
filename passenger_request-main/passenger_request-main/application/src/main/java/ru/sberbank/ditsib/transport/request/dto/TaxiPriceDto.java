package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

/**
 * Оценочные данные стоимости поездки
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Оценочные данные стоимости поездки",
        description = "Оценочные данные стоимости поездки")
public class TaxiPriceDto {
    
    /**
     * Провайдер услуг
     */
    private String provider;
    
    /**
     * Номер тарифа
     */
    @Builder.Default
    private String tariffId = "";
    
    /**
     * Номер тарифа
     */
    private TaxiClass taxiClass;
    
    /**
     * Цена поездки
     */
    @Builder.Default
    private Integer price = 0;
    
    /**
     * Хеш расчета
     */
    @Builder.Default
    private String calcHash = "";
    
    /**
     * Приблизительное время прибытия estimated time of arrival (секунды)
     */
    @Builder.Default
    private Integer eta = 0;
    
    /**
     * Приблизительная продолжительность поездки (секунды)
     */
    @Builder.Default
    private Integer duration = 0;
}
