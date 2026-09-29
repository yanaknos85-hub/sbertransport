package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.settings.CountingRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.services.NotificationCountingService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с количественным триггером.
 */
@RequiredArgsConstructor
@Transactional
@Component
class NotificationCountingServiceImpl implements NotificationCountingService {
    
    private final NotificationSettingsRepository notificationSettingsRepository;
    
    private final CountingRepository countingRepository;
    
    @Override
    public List<CountingSettings> getCountingOf(
            UUID organizationId, UUID id
                                               ) {
        return notificationSettingsRepository.findByParentIdAndId(organizationId, id)
                                             .map(NotificationSettings::getCountings)
                                             .orElseThrow(() -> new EntityNotFoundException(NotificationSettings.class, id));
    }
    
    @Override
    public void setCountingOf(
            UUID organizationId, UUID id,
            List<? extends CountingSettings> data
                             ) {
        var notification = notificationSettingsRepository.findByParentIdAndId(organizationId, id)
                                                         .orElseThrow(
                                                                 () -> new EntityNotFoundException(NotificationSettings.class, id));

        var countings = notification.getCountings();
        countingRepository.deleteAllByIdInBatch(countings.stream().map(CountingSettings::getId).toList()); // NOSONAR
        countings.clear();
        List<CountingSettings> list = new ArrayList<>();
        for (var item : data) {
            item.setNotification(notification);
            var save = countingRepository.save(item);
            list.add(save);
        }
        data = list;

        countings.addAll(data);
    }
}
