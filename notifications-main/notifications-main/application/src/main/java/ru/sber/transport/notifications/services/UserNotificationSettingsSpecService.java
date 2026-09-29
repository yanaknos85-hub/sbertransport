package ru.sber.transport.notifications.services;

import org.springframework.data.jpa.domain.Specification;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;


public interface UserNotificationSettingsSpecService<T extends UserNotificationSettingsSearchDto> {
    /**
     * Получение фильтров спецификаций для поиска уведомлений в БД
     *
     * @param userNotificationSettingsSearchDto данные для поиска
     * @return спецификации заявок для класса уведомлений notificationClass
     */
    Specification<UserNotificationSettings> getSpec(T userNotificationSettingsSearchDto);

}