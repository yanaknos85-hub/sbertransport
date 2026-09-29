package ru.sber.transport.notifications.services;

import org.springframework.data.domain.Page;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;

import java.util.Map;
import java.util.UUID;

/**
 * Сервис для работы с настройками уведомлений пользователя для ЛК
 */
public interface UserNotificationSettingsService {

    /**
     * Получение настроек пользователя с пагинацией.
     * Если обращение происходит в первые - настроек нет.
     * Для создания настроек выполняется запрос всех настроек для организации
     * с учетом класса уведомлений, затем они преобразуются в настройки пользовательских уведомлений,
     * сохраняются в БД, после чего возвращается первая страница.
     *
     * Первый запрос может выполняться долго.
     *
     * @return страница настроек пользовательских уведомлений
     */
    Page<UserNotificationSettingsDto> get(UserNotificationSettingsSearchDto search);

    /**
     * Поиск настроек пользовательских уведомлений для ЛК с пагинацией.
     *
     * @param searchDto параметры поиска
     * @return результат поиска
     */
    Page<UserNotificationSettingsDto> search(UserNotificationSettingsSearchDto searchDto);

    /**
     * Обновление настроек
     * @param id ИД настройки уведомления
     * @param userNotificationSettings обновленные настройки
     * @return результат после обновления
     */
    UserNotificationSettingsDto updateByUser(UUID id, UserNotificationSettingsDto userNotificationSettings);

    /**
     * Сохранение или обновление при изменении инженером в корп клиенте базовых настроек уведомлений
     */
    void saveByEngineerCorpClient(NotificationSettings result);

    void updateChannelMap(HasContactData receiver, Map<ChannelType, String> messages, UUID  notificationId);

}
