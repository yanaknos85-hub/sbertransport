package ru.sberbank.ditsib.transport.reports.pojo;

import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;

/**
 * Класс для проверки Реестра поездок на такси от контрагента
 */
public interface TaxiTripRegistryChecker {
    
    /**
     * Осуществляет проверки строк №0 - №8 и заполняет соответствующие поля
     * @param registry Реестр поездок на такси от контрагента
     */
    void checkStrings(TaxiTripRegistry registry);
    
}
