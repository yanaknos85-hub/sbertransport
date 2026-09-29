package ru.sber.transport.notifications.database.dao.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с таймингом.
 */
public interface CountingRepository extends JpaRepository<CountingSettings, UUID> {
    
    List<CountingSettings> findAllByPropertyName(String propertyName);
    
    List<CountingSettings> findAllByNotification(NotificationSettings settings);
}
