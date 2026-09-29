package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.settings.DefaultNotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DefaultNotificationSettingsService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultNotificationSettingsServiceImpl implements DefaultNotificationSettingsService {
    
    private final DefaultNotificationSettingsRepository repository;
    
    @Override
    public List<NotificationSettings> getDefaultSettings() {
        return repository.findAllDefaultSettings();
    }
    
    @Override
    public NotificationSettings getSettingByClassAndType(
            NotificationClass notificationClass, NotificationType notificationType
                                                         ) {
        return repository.findSettingByClassAndType(notificationClass, notificationType);
    }
}
