package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.*;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект с данными из файла с тарифами личного транспорта.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PersonalFileDto implements TariffFileDto {
    
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Организация.
     */
    @NullRender
    private String organization;
    
    /**
     * Тип услуги.
     */
    private String serviceType;
    
    /**
     * Регион.
     */
    @NotBlank
    @NullRender
    private String region;
    
    /**
     * Включено в тариф.
     */
    @Builder.Default
    private IncludesFileDto includes = new IncludesFileDto();
    
    /**
     * Данные цен.
     */
    @Builder.Default
    private CostFileDto cost = new CostFileDto();
    
    /**
     * Коэффициент.
     */
    @Builder.Default
    private CoefficientFileDto coefficients = new CoefficientFileDto();
    
    /**
     * Отклонения.
     */
    @Builder.Default
    private CoopDeviationFileDto coops = new CoopDeviationFileDto();
    
}
