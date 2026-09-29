package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.*;

/**
 * Объект данных коэффициентов.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CoefficientFileDto {
    
    /**
     * Страховка.
     */
    @Builder.Default
    private double insurance = 1;
    
    /**
     * Утро (07:00 - 10:00).
     */
    @Builder.Default
    private double morning = 1;
    
    /**
     * День (10:00 - 18:00).
     */
    @Builder.Default
    private double day = 1;
    
    /**
     * Вечер (18:00 - 22:00).
     */
    @Builder.Default
    private double evening = 1;
    
    /**
     * Ночь (22:00 - 07:00).
     */
    @Builder.Default
    private double night = 1;
    
    /**
     * Выходные.
     */
    @Builder.Default
    private double weekend = 1;
    
    /**
     * Дети (детское кресло).
     */
    @Builder.Default
    private double children = 1;
    
    /**
     * Животные.
     */
    @Builder.Default
    private double pet = 1;
    
    /**
     * Трафик (загруженность дорог).
     */
    @Builder.Default
    private double traffic = 1;
    
    /**
     * Коэффициент за ТМЦ.
     */
    @Builder.Default
    private double goods = 1;
    
    /**
     * За негабарит.
     */
    @Builder.Default
    private double oversized = 1;
    
    /**
     * За организации.
     */
    @Builder.Default
    private double organization = 1;
    
    /**
     * Сезонный коэффициент.
     */
    @Builder.Default
    private SeasonFileDto season = SeasonFileDto.builder().build();
    
    /**
     * Коэффициент за двигатель.
     */
    @Builder.Default
    private EngineFileDto engine = EngineFileDto.builder().build();
    
    /**
     *  Индекс затрат на страхование, руб./км
     */
    @Builder.Default
    private double trustIdx = 0.0;
    
}
