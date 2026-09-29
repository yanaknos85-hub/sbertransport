package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.NotificationContact;

import java.util.Optional;
import java.util.UUID;

public interface NotificationContactService {

    /**
     * Получение контакта.
     *
     * @param id идентификатор контакта.
     * @return контакт.
     */
    Optional<NotificationContact> get(UUID id);

    /**
     * Сохранение контакта.
     *
     * @param contact контакт.
     * @return сохраненный контакт.
     */
    NotificationContact save(NotificationContact contact);

    /**
     * Удаление контакта
     * @param contactId id контакта
     */
    void deleteById(UUID contactId);

    /**
     * Поиск HasContactData по id
     * @param id id контакта
     * @return найденный контакт
     */
    Optional<HasContactData> findContactDataById(UUID id);

}
