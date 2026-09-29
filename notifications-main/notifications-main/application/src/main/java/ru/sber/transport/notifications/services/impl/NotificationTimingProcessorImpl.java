package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.TimingService;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;
import ru.sber.transport.utils.collections.MapUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Реализация процессора уведомлений с настройками отправки по времени.
 */
@Component
@Slf4j
@Transactional
class NotificationTimingProcessorImpl extends BaseNotificationSettingsProcessor<TimingSettings> {
    
    private final TimingService timingService;

    private final ObjectMapper objectMapper;

    private final MapUtils mapUtils;

    public NotificationTimingProcessorImpl(
            NotificationService notificationService,
            NotificationSender sender,
            TimingService timingService,
            List<JpaRepository<?, ?>> repositories,
            ObjectMapper objectMapper,
            UserNotificationSettingsService userSettingsService, MapUtils mapUtils,
            TransactionTemplate transactionTemplate) {
        super(repositories, notificationService, sender, objectMapper, userSettingsService, transactionTemplate);
        this.timingService = timingService;
        this.objectMapper = objectMapper;
        this.mapUtils = mapUtils;
    }

    @Override
    protected Iterable<Notification> getNotifications(NotificationService notificationService) {
        return notificationService.getTimingNotSent();
    }

    @Override
    protected Iterable<TimingSettings> getSendSettings(NotificationSettings settings) {
        return timingService.getOfSettings(settings);
    }

    @Override
    protected void doProcess(Notification notification, TimingSettings timing) throws JsonProcessingException {
        var entity = notification.getEntity();
        log.debug("Start processing timed notification for entity {}", entity);
        var settings = notification.getSettings();
        HasContactData receiver;
        if (notification.getCustomer() == null && notification.getReceiverId() != null) {
            receiver = getReceiver(notification.getReceiverId(), settings.getParentType());
        } else if (notification.getCustomer() != null) {
            receiver = notification.getCustomer();
        } else {
            throw new IllegalStateException("notification " + notification.getId() + "does not contain customer or receiverId");
        }

        var type = timing.getType();
        var before = timing.getTimeBefore();
        var fieldName = timing.getTimeFieldName();
        var deadLineFieldName = timing.getDeadlineFieldName();
        var entityObject = objectMapper.readValue(entity, Object.class);
        var time = mapUtils.extractNode(entityObject, fieldName, LocalDateTime.class);
        var deadline = mapUtils.extractNode(entityObject, deadLineFieldName, LocalDateTime.class);
        var now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        var notificationType = settings.getType();
        switch (type) {
            case AT_EVENT -> {
                if (time == null || Duration.between(time, now).toSeconds() >= before.toSeconds()) {
                    send(notification, receiver, notificationType);
                }
                return;
            }
            case BEFORE_DEADLINE -> {
                if (deadline == null || Duration.between(deadline, now).abs().toSeconds() <= before.toSeconds()) {
                    send(notification, receiver, notificationType);
                } else if(Duration.between(deadline, now).toSeconds() > 0) {
                    setSentWithoutSendingMessage(notification);
                }
                return;
            }
            default -> log.debug("Unknown send timing type %s".formatted(type));
        }
        log.debug("There is no time for send %s".formatted(entity));
    }
}
