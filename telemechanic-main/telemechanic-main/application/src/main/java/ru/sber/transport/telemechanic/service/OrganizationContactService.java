package ru.sber.transport.telemechanic.service;

import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;

import java.util.List;
import java.util.UUID;

/**
 * Сервис по работе с связями контактных данных и организаций
 */
public interface OrganizationContactService {
    
    /**
     * Сохранение списка контактов для выбранной организации
     *
     * @param organizationId Идентификатор записи об организации
     * @param contacts {@link List<ContactMessage>}
     */
    void save(UUID organizationId, List<ContactMessage> contacts);
    
    /**
     * Удаление списка контактов для выбранной организации
     *
     * @param organizationId Идентификатор записи об организации
     */
    void deleteAll(UUID organizationId);
}
