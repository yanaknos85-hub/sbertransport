package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;

/**
 * Объект с данными из файла с тарифами такси.
 */
@Getter
@Setter
public class TaxiFileDto implements TariffContractFileDto {
    
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Договор.
     */
    @NotBlank
    private String contract;
    
    /**
     * Контрагент.
     */
    @NotBlank
    private String contractor;
    
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
     * Класс такси.
     */
    @NotBlank
    private String taxiClass;
    
    private String departmentHumanReadableId;
    
    /**
     * Включено в тариф.
     */
    private IncludesFileDto includes = new IncludesFileDto();
    
    /**
     * Данные цен.
     */
    private CostFileDto cost = new CostFileDto();
    
    /**
     * Коэффициент.
     */
    private CoefficientFileDto coefficients = new CoefficientFileDto();
    
    /**
     * Отклонения.
     */
    private CoopDeviationFileDto coops = new CoopDeviationFileDto();
    
    /**
     * Отклонения контрагента.
     */
    private ContractorDeviationsFileDto deviations = new ContractorDeviationsFileDto();
    
    /**
     * Тип интеграции.
     */
    private String integrationType;
    
    /**
     * Тариф контрагента.
     */
    private String contractorTariffId;
    
    /**
     * Тип услуги.
     */
    private String serviceType;
    
    /**
     * Рабочая группа.
     */
    private String workGroup;
    
    /**
     * Признак "Ночной тариф"
     */
    private boolean isNightTariff = false;
}
