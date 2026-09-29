package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Объект с данными о коэффициентах за двигатель.
 */
@Setter
@Getter
@Builder
@ToString
public class EngineFileDto {
    
    /**
     * Маленький двигатель (менее 1.6).
     */
    @Builder.Default
    private double small = 1;
    
    /**
     * Нормальный двигатель (1.6 - 2.0).
     */
    @Builder.Default
    private double medium = 1;
    
    /**
     * Большой двигатель (2.0 - 2.5).
     */
    @Builder.Default
    private double large = 1;
}
