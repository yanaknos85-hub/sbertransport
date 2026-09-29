package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import io.qameta.allure.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.notifications.database.dao.settings.ChannelSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.TimingRepository;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.Duration;
import java.util.UUID;

@SpringBootTest(properties = {"spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=false"} )
@Feature("app_platform_notifications")
@EmbeddedPostgres
public class GroupTransferCommon {

    @Autowired
    private NotificationSettingsRepository settingsRepository;

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    @Autowired
    private TimingRepository timingRepository;

    protected void create_or_update_notification_with_send_time(
            String p_class,
            String p_name,
            String p_description,
            String p_parent_id,
            String p_owner_id,
            String p_type,
            String p_text,
            String p_parent_type,
            String p_text_channel,
            boolean p_push_active,
            boolean p_sms_active,
            boolean p_email_active,
            long p_time_before,
            String p_type_send_time,
            String p_field_name,
            String p_deadline_field_name
    ) {
        var settings = new NotificationSettings();
//            settings.setOwnerId(departmentHeadId);
        settings.setDescription(p_description);
        settings.setName(p_name);
        settings.setNotificationClass(NotificationClass.valueOf(p_class));
        settings.setType(NotificationType.valueOf(p_type));
        settings.setParentType(NotificationSettings.ParentType.ORGANIZATION);
        settings.setParentId(UUID.fromString(p_parent_id));
        settings = settingsRepository.save(settings);

        var channelSettings = new ChannelSettings();
        channelSettings.setActive(p_sms_active);
        channelSettings.setText(p_text_channel);
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setNotification(settings);
        channelSettingsRepository.save(channelSettings);

        channelSettings = new ChannelSettings();
        channelSettings.setActive(p_email_active);
        channelSettings.setText(p_text_channel);
        channelSettings.setChannel(ChannelType.EMAIL);
        channelSettings.setNotification(settings);
        channelSettingsRepository.save(channelSettings);

        channelSettings = new ChannelSettings();
        channelSettings.setActive(p_push_active);
        channelSettings.setText(p_text_channel);
        channelSettings.setChannel(ChannelType.PUSH);
        channelSettings.setNotification(settings);
        channelSettingsRepository.save(channelSettings);

        var timingSettings = new TimingSettings();
        timingSettings.setTimeBefore(Duration.ofNanos(p_time_before));
        timingSettings.setDeadlineFieldName(p_deadline_field_name);
        timingSettings.setTimeFieldName(p_field_name);
        timingSettings.setType(EventType.valueOf(p_type_send_time));
        timingSettings.setNotification(settings);
        timingRepository.save(timingSettings);

    };
}
