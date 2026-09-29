package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Объект с данными из файла с тарифами трансфера.
 */
@Getter
@Setter
public class TransferFileDto implements TariffContractFileDto {
    /**
     * Идентификатор.
     */
    private String id;
    
    /**
     * Активность тарифа.
     */
    private boolean active;
    
    /**
     * Статус тарифа.
     */
    private String tariffStatus;
    
    /**
     * Наименование контрагента.
     */
    private String contractor;
    
    /**
     * Номер договора.
     */
    private String contract;
    
    /**
     * Организация заказчика.
     */
    private String organization;
    
    /**
     * Регион/геозона.
     */
    private String region;
    
    /**
     * Подразделение.
     */
    private String departmentHumanReadableId;
    
    /**
     * Тип интеграции.
     */
    private String integrationType;
    
    /**
     * Период действия тарифа с.
     */
    private String tariffPeriodFrom;
    
    /**
     * Период действия тарифа по.
     */
    private String tariffPeriodTo;
    
    /**
     * Тип услуги.
     */
    private String serviceType;
    
    /**
     * Класс тарифа.
     */
    private String tariffClass;
    
    /**
     * Рабочая группа.
     */
    private String workGroup;
    
    /**
     * Атрибуты для минимальной поездки/подачи ТС.
     */
    private MinimumTripAttributesFileDto minimumTripAttributes = new MinimumTripAttributesFileDto();
    
    /**
     * Общие значения тарифа.
     */
    private GeneralTariffValuesFileDto generalTariffValues = new GeneralTariffValuesFileDto();
    
    /**
     * Расширенные значения тарифа.
     */
    private ExtendedTariffValuesFileDto extendedTariffValues = new ExtendedTariffValuesFileDto();
    
    /**
     * Условия.
     */
    private ConditionsFileDto conditions = new ConditionsFileDto();
}
