package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.NotificationTimingService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с таймингом.
 */
@RequiredArgsConstructor
@Transactional
@Component
class NotificationTimingServiceImpl implements NotificationTimingService {
    
    private final NotificationSettingsRepository notificationSettingsRepository;
    
    private final TimingRepository timingRepository;
    
    @Override
    public List<TimingSettings> getTimingOf(
            UUID organizationId, UUID id
                                           ) {
        return notificationSettingsRepository.findById(id)
                                             .map(NotificationSettings::getTimings)
                                             .orElseThrow(() -> new EntityNotFoundException(NotificationSettings.class, id));
    }
    
    @Override
    public void setTimingOf(
            UUID organizationId, UUID id,
            List<? extends TimingSettings> data
                           ) {
        var notification = notificationSettingsRepository.findById(id)
                                                         .orElseThrow(
                                                                 () -> new EntityNotFoundException(NotificationSettings.class, id));
        
        var timings = notification.getTimings();
        timingRepository.deleteAllByIdInBatch(timings.stream().map(TimingSettings::getId).toList()); // NOSONAR
        timings.clear();
        List<TimingSettings> list = new ArrayList<>();
        for (TimingSettings item : data) {
            item.setNotification(notification);
            TimingSettings save = timingRepository.save(item);
            list.add(save);
        }
        data = list;
        timings.addAll(data);
    }
}
