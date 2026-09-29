package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.notifications.database.model.*;

import java.util.Optional;

/**
 * Маппер для маппинга данных о контактах
 */
@Mapper
public interface NotificationContactMapper {

    /**
     * Замапить данные о контакте с почтой
     * @param notificationsContact entity
     * @return данные о контакте с почтой
     */
    ContactWithEmail mapContactWithEmail(NotificationContact notificationsContact);

    /**
     * Замапить данные о контакте с почтой
     * @param notificationsContact entity
     * @return данные о контакте с телефоном
     */
    ContactWithPhone mapContactWithPhone(NotificationContact notificationsContact);

    /**
     * Замапить данные о контакте с почтой
     * @param notificationsContact entity
     * @return данные о контакте с телефоном и почтой
     */
    ContactWithPhoneAndEmail mapContactWithPhoneAndEmail(NotificationContact notificationsContact);

    /**
     * В зависимости от наличия данных о контакте с телефоном и почтой выбрать реализацию
     * @param contact entity
     * @return данные о контакте
     */
    default Optional<HasContactData> toContactData(NotificationContact contact) {
        if (contact.getPhone() != null && contact.getEmail() != null) {
            return Optional.of(mapContactWithPhoneAndEmail(contact));
        } else if (contact.getPhone() != null) {
            return Optional.of(mapContactWithPhone(contact));
        } else if (contact.getEmail() != null) {
            return Optional.of(mapContactWithEmail(contact));
        } else {
            return Optional.empty();
        }
    }
}
