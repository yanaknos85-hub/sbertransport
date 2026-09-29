package ru.sber.transport.telemechanic.service;

import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;

import java.util.List;

/**
 * Сервис по работе с контактными данными
 */
public interface ContactService {
    
    /**
     * Сохранение списка контактных данных
     *
     * @param contacts {@link List<ContactMessage>}
     */
    void saveAll(List<ContactMessage> contacts);
    
    /**
     * Удаление списка контактных данных
     *
     * @param contacts {@link List<ContactMessage>}
     */
    void deleteAll(List<ContactMessage> contacts);
    
    /**
     * Удаление всех неиспользуемых контактов
     */
    void deleteAllUnused();
}
