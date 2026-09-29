package ru.sber.transport.notifications.database.dao.userNotificationSettings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, UUID>,
        JpaSpecificationExecutor<UserNotificationSettings> {

    List<UserNotificationSettings> findByParentIdAndNotificationId(UUID organizationId, UUID notificationId);

    @Query("SELECT distinct u.userId  FROM UserNotificationSettings u WHERE u.parentId = ?1 Group By parentId, userId ")
    Set<UUID> findByParentId(UUID parentId);


    Optional<UserNotificationSettings> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    @Query(nativeQuery = true, value = "with org_settigns as (" +
            " select * from notifications_settings.notification n where n.parent_id = :parentId and n.class = :notificationClass " +
            "), user_settings as (" +
            " select * from notifications_settings.user_notification_settings uns  where uns.parent_id = :parentId and uns.class = :notificationClass and uns.user_id = :userId " +
            ")" +
            " select org_settigns.* from org_settigns " +
            " left join user_settings on org_settigns.id = user_settings.notification_id" +
            " where user_settings.id is null")
    List<NotificationSettings> findMissedSettings(UUID userId, UUID parentId, String notificationClass);
}
