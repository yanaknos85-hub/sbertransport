package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект с данными из файла с тарифами каршеринга.
 */
@Getter
@Setter
public class CarsharingFileDto implements TariffContractFileDto {
    
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Контрагент..
     */
    @NullRender
    private String contractor;
    
    /**
     * Договор.
     */
    @NotBlank
    private String contract;
    
    /**
     * Организация.
     */
    @NotBlank
    private String organization;
    
    /**
     * Регион.
     */
    @NotBlank
    private String region;
    
    /**
     * Данные цен.
     */
    private CostFileDto cost = new CostFileDto();
    
    /**
     * Коэффициент.
     */
    private CoefficientFileDto coefficients = new CoefficientFileDto();
    
    /**
     * Тип сервиса.
     */
    private String serviceType;

}
