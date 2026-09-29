package ru.sber.transport.notifications.database.model.settings.timing;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sber.transport.notifications.database.model.settings.SendSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import jakarta.persistence.*;

import java.time.Duration;
import java.util.UUID;

/**
 * Настройки тайминга отправки..
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "notifications_settings", name = "send_time")
public class TimingSettings implements SendSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "field_name")
    private String timeFieldName;

    @Column(name = "deadline_field_name")
    private String deadlineFieldName;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "time_before")
    private Duration timeBefore;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private NotificationSettings notification;

    public TimingSettings(TimingSettings timingSettings) {
        this(null, timingSettings.getTimeFieldName(), timingSettings.getDeadlineFieldName(), timingSettings.getTimeBefore(), timingSettings.getType(), null);
    }
}
