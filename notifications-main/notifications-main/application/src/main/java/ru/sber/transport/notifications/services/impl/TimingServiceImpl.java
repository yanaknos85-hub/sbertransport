package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.TimingService;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Реализация сервиса работы с настройками тайминга.
 */
@RequiredArgsConstructor
@Transactional
@Component
class TimingServiceImpl implements TimingService {
    
    private final TimingRepository repository;
    
    @Override
    public List<TimingSettings> getOfSettings(
            NotificationSettings settings
                                             ) {
        return repository.findAllByNotification(settings);
    }
}
