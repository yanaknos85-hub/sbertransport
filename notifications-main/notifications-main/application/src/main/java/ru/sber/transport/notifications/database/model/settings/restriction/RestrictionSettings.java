package ru.sber.transport.notifications.database.model.settings.restriction;

import lombok.*;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сущность ограничений уведомлений.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "notifications_settings", name = "restrictions")
@ToString(exclude = "notification")
public class RestrictionSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restrict_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RestrictType restrictType = RestrictType.ALLOW_ALL;

    @OneToMany(mappedBy = "restrictionSettings", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<RestrictionRoles> roles = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "settings_id", nullable = false)
    private NotificationSettings notification;

    public RestrictionSettings(NotificationSettings notificationSettings) {
        this.notification = notificationSettings;
    }

    public RestrictionSettings(RestrictionSettings restrictions) {
        this(null, Optional.ofNullable(restrictions).map(RestrictionSettings::getRestrictType).orElse(null), Optional.ofNullable(restrictions).map(RestrictionSettings::getRoles).orElse(List.of()).parallelStream().map(RestrictionRoles::new).toList(), null);
    }

}
