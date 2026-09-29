package ru.sber.transport.notifications.database.dao.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с таймингом.
 */
public interface TimingRepository extends JpaRepository<TimingSettings, UUID> {
    
    List<TimingSettings> findAllByNotification(NotificationSettings settings);
    
}
