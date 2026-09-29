package ru.sber.transport.notifications.database.model.settings.userNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(schema = "notifications_settings", name = "user_notification_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id", "notification_id", "user_id" }))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность пользовательских настроек уведомлений для личного кабинета")
public class UserNotificationSettings {
    @Id
    @GeneratedValue
    private UUID id;
    @Schema(description = "Идентификатор пользователя")
    private UUID userId;
    @Schema(description = "Идентификатор настроек уведомления")
    private UUID notificationId;
    @Schema(description = "Класс уведомления")
    @Column(name = "class", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationClass notificationClass;
    @Schema(description = "Название уведомления")
    private String name;
    @Schema(description = "Активность push-уведомлений в ЛК")
    private boolean pushActive;
    @Schema(description = "Доступность push-уведомления для организации")
    private boolean pushEnabled;
    @Schema(description = "Активность email-уведомлений в ЛК")
    private boolean emailActive;
    @Schema(description = "Доступность email-уведомления для организации")
    private boolean emailEnabled;
    @Schema(description = "Активность sms-уведомлений в ЛК")
    private boolean smsActive;
    @Schema(description = "Доступность sms-уведомления для организации")
    private boolean smsEnabled;
    @Schema(description = "Идентификатор организации или контрагента")
    private UUID parentId;
}
