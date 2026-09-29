package ru.sber.transport.notifications.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.services.NotificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса по работе с уведомлениями.
 */
@RequiredArgsConstructor
@Transactional
@Component
@Slf4j
class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public Notification save(Notification notification) {
        NotificationSettings settings = notification.getSettings();
        String settingsName = settings != null ? settings.getName() : "null";
        log.debug("NotificationServiceImpl.save() notification.id={}, notificationSettings.name={}", 
                notification.getId(), settingsName);
        try {
            return repository.save(notification);
        } catch (Exception e) {
            log.error("NotificationServiceImpl.save() failed for notification id: {}, receiverId: {}",
                    notification.getId(), notification.getReceiverId(), e);
            throw e;
        }
    }

    @Override
    public List<Notification> getCountingNotSent() {
        return repository.findAllBySentIsFalseAndWithCountingSettings();
    }

    @Override
    public List<Notification> getTimingNotSent() {
        return repository.findAllBySentIsFalseAndWithTimingSettings();
    }

    @Override
    public Notification get(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public void cancelAll(
            NotificationSettings notificationSettings
                         ) {
        repository.deleteAllBySettings(notificationSettings);
    }

    @Override
    public Optional<Notification> find(@NonNull UUID entityId) {
        return repository.findByEntityId(entityId);
    }
}
