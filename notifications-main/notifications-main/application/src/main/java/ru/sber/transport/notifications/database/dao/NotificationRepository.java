package ru.sber.transport.notifications.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий уведомлений.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Поиск всех неотправленных уведомлений.
     *
     * @return список уведомлений.
     */
    @Query("SELECT notification FROM Notification notification INNER JOIN notification.settings settings " +
           "INNER JOIN settings.timings as timings WHERE notification.sent = FALSE and notification.sentError = FALSE")
    List<Notification> findAllBySentIsFalseAndWithTimingSettings();
    
    /**
     * Поиск всех неотправленных уведомлений.
     *
     * @return список уведомлений.
     */
    @Query("SELECT notification FROM Notification notification INNER JOIN notification.settings settings " +
           "INNER JOIN settings.countings as countings WHERE notification.sent = FALSE  and notification.sentError = FALSE")
    List<Notification> findAllBySentIsFalseAndWithCountingSettings();
    
    void deleteAllBySettings(NotificationSettings notificationSettings);

    Optional<Notification> findByEntityId(UUID entityId);
}
