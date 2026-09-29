package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.ContractorType;

/**
 * Фабрика сервисов интеграции
 */
public interface IntegrationServiceFactory {

    /**
     * Получение сервиса по типу контрактора
     *
     * @param contractorType тип котрактора
     * @return сервис
     */
    IntegrationService getService(ContractorType contractorType);

}
