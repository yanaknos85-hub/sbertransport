package ru.sberbank.ditsib.provider;

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
    
}
