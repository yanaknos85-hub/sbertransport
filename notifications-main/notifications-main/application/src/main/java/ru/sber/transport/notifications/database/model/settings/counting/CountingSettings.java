package ru.sber.transport.notifications.database.model.settings.counting;

import lombok.*;
import ru.sber.transport.notifications.database.model.settings.SendSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Настройки тайминга отправки..
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "notifications_settings", name = "send_count")
public class CountingSettings implements SendSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "count", nullable = false)
    private Double count;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CountingType type;

    @Column(name = "property_name", nullable = false)
    private String propertyName;

    @Column(name = "initial_property_name", nullable = false)
    private String initialPropertyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private NotificationSettings notification;

    public CountingSettings(CountingSettings countingSettings) {
        this(null, countingSettings.getCount(), countingSettings.getType(), countingSettings.getPropertyName(), countingSettings.getInitialPropertyName(), null);
    }
}
