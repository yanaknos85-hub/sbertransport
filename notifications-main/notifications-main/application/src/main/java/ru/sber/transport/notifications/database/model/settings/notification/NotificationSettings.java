package ru.sber.transport.notifications.database.model.settings.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сущность уведомлений.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(schema = "notifications_settings", name = "notification",
       uniqueConstraints = @UniqueConstraint(columnNames = { "parent_id", "type", "class", "owner_id" }))
public class NotificationSettings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "description")
    private String description;
    
    @Column(name = "class", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationClass notificationClass;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_type", nullable = false)
    @Builder.Default
    private ParentType parentType = ParentType.ORGANIZATION;
    
    @Column(name = "owner_id")
    private UUID ownerId;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "notification", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private final List<ChannelSettings> channels = new ArrayList<>();

    @BatchSize(size=100)
    @OneToMany(mappedBy = "notification", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private final List<TimingSettings> timings = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "notification", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private final List<CountingSettings> countings = new ArrayList<>();

    @OneToOne(mappedBy = "notification", orphanRemoval = true, cascade = CascadeType.ALL)
    private RestrictionSettings restrictions =  new RestrictionSettings(this);

    public NotificationSettings(NotificationSettings setting) {
        this(
                null,
                setting.name,
                setting.text,
                setting.description,
                setting.notificationClass,
                setting.type,
                setting.parentId,
                setting.parentType,
                setting.ownerId,
                setting.channels.stream().map(ChannelSettings::new).toList(),
                setting.timings.stream().map(TimingSettings::new).toList(),
                setting.countings.stream().map(CountingSettings::new).toList(),
                Optional.ofNullable(setting.restrictions).map(RestrictionSettings::new).orElse(null)
        );
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ParentType {
        ORGANIZATION(Employee.class),
        CONTRACTOR(Driver.class);

        private final Class<? extends HasContactData> receiverClass;
    }
    
}
