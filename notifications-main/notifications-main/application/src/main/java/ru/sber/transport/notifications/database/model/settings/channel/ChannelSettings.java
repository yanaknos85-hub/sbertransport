package ru.sber.transport.notifications.database.model.settings.channel;

import lombok.*;
import ru.sber.transport.notifications.database.model.settings.SendSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Настройки уведомлений.
 */
@Entity
@Table(schema = "notifications_settings", name = "channel")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ChannelSettings implements SendSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType channel;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private NotificationSettings notification;

    public ChannelSettings(ChannelSettings channelSettings) {
        this(null, channelSettings.getChannel(), channelSettings.getText(), channelSettings.isActive(), null);
    }
}
