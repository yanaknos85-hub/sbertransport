package ru.sber.transport.notifications.mapper.userNotificationSettings;

import org.mapstruct.Mapper;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;

@Mapper
public interface UserNotificationSettingsMapper {

    UserNotificationSettingsDto toDto(UserNotificationSettings userNotificationSettings);
}
