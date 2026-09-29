package ru.sberbank.ditsib.transport.tariff.dto.files;

/**
 * Интерфейс тарифов с договорами.
 */
public interface TariffContractFileDto extends TariffFileDto {
    
    /**
     * Установить договор.
     *
     * @param contract номер или наименование договора.
     */
    void setContract(String contract);
    
    /**
     * Установить контрагента.
     *
     * @param contractor номер или наименование контрагента.
     */
    void setContractor(String contractor);
    
}
