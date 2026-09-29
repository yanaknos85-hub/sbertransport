package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.services.CountingService;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Реализация сервиса работы с настройками количественных триггеров.
 */
@RequiredArgsConstructor
@Transactional
@Component
class CountingServiceImpl implements CountingService {
    
    private final CountingRepository repository;
    
    @Override
    public List<CountingSettings> getOfSettings(
            NotificationSettings settings
                                               ) {
        return repository.findAllByNotification(settings);
    }
}
