package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Поставщик данных организаций.
 */
public interface OrganizationProvider {
    
    /**
     * Удаление организации.
     *
     * @param message данные организации для удаления.
     */
    void delete(OrganizationMessage message);
    
    /**
     * Сохранение организации.
     *
     * @param message данные организации для сохранения.
     */
    void save(OrganizationMessage message);
    
    /**
     * Привязка контрагента исполняющего функцию внутреннего автопарка к ораганизации
     * @param contractorMessage данные контрагента
     */
    void addContractor(ContractorMessage contractorMessage);
    
    /**
     * Удаление привязки контрагента к ораганизаци
     * @param contractorMessage данные контргагнта
     */
    void deleteContractor(ContractorMessage contractorMessage);
    
}
