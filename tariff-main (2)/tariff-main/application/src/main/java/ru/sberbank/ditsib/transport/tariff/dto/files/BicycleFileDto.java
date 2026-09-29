package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект данных тарифа на велосипед из файла.
 */
@Getter
@Setter
public class BicycleFileDto implements TariffContractFileDto {
    
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
    private String contractor;
    
    /**
     * Договор.
     */
    private String contract;
    
    /**
     * Организация.
     */
    @NotBlank
    @NullRender
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

}
