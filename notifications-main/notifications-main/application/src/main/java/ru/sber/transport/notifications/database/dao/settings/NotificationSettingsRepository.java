package ru.sber.transport.notifications.database.dao.settings;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings_;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозитории для работы с настройками уведомлений.
 */
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, UUID> {

    /**
     * Проверка существования настроек по id владельца уведомления.
     *
     * @param id идентификатор уведомления.
     * @return <code>true</code> если запись есть.
     */
    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    Optional<NotificationSettings> findByOwnerIdAndIdAndNotificationClass(UUID ownerId, UUID id, NotificationClass notificationClass);

    /**
     * Проверка существования настроек по имени.
     *
     * @param description техническое наименование уведомления.
     * @return <code>true</code> если запись есть.
     */
    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    Optional<NotificationSettings> findByParentIdAndDescription(UUID organizationId, String description);

    /**
     * Проверка существования настроек по имени.
     *
     * @param description техническое наименование уведомления.
     * @param id          идентификатор для исключения из поиска.
     * @return <code>true</code> если запись есть.
     */
    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    Optional<NotificationSettings> findByParentIdAndDescriptionAndIdNot(UUID organizationId, String description, UUID id);

    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    @NonNull
    List<NotificationSettings> findAllByParentIdOrderByName(UUID organizationId);

    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    @NonNull
    List<NotificationSettings> findAllByOwnerIdAndNotificationClassOrderByName(UUID ownerId, NotificationClass notificationClass);

    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    @NonNull
    List<NotificationSettings> findByParentIdAndNotificationClassAndTypeAndOwnerId(
            UUID parentId,
            NotificationClass notificationClass,
            NotificationType type,
            UUID ownerId
    );

    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    Optional<NotificationSettings> findByParentIdAndId(UUID organizationId, UUID id);

    @EntityGraph(attributePaths = {NotificationSettings_.CHANNELS, NotificationSettings_.RESTRICTIONS})
    List<NotificationSettings> findByParentIdAndNotificationClassOrderByNameAsc(UUID organizationId, NotificationClass notificationClass);

    /**
     * Поиск настроек по id получателя и типу уведомления.
     *
     * @param receiverId id получателя
     * @param notificationType тип сообщения об уведомлении
     * @return настройки нотификаций
     */
    @Query(nativeQuery = true, value = """
            SELECT s.*
            FROM notifications_settings.notification s
            JOIN notifications_corporate.employee e ON s.parent_id = e.organization_id
            WHERE e.id = :receiverId AND s.type = :notificationType AND s.parent_type = 'ORGANIZATION'
            """)
    List<NotificationSettings> findAllByReceiverIdAndNotificationType(UUID receiverId, String notificationType);
}
