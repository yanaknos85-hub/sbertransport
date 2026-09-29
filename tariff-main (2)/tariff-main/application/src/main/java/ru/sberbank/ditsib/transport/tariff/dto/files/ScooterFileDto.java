package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект данных тарифа на скутер из файла.
 */
@Getter
@Setter
public class ScooterFileDto implements TariffContractFileDto {
    
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Контрагент.
     */
    @NullRender
    private String contractor;
    
    /**
     * Договор.
     */
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
    @NullRender
    private String region;
    
    /**
     * Данные цен.
     */
    private CostFileDto cost = new CostFileDto();
    
    /**
     * Коэффициент.
     */
    private CoefficientFileDto coefficients = new CoefficientFileDto();

}
