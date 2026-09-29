package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект с данными тарифа за общественный транспорт.
 */
@Getter
@Setter
public class PublicFileDto implements TariffFileDto {
    
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
    private String organization;
    
    /**
     * Регион.
     */
    @NotBlank
    @NullRender
    private String region;
    
    /**
     * Тип услуги.
     */
    @NullRender
    private String serviceType;

    /**
     * Цены тарифов
     */
    private PublicCostDto cost;
}
