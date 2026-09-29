package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.services.CountingService;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;
import ru.sber.transport.utils.collections.MapUtils;

import java.util.List;
import java.util.Objects;

/**
 * Реализация процессора уведомлений с настройками отправки по количеству.
 */
@Component
@Slf4j
class NotificationCountingProcessorImpl extends BaseNotificationSettingsProcessor<CountingSettings> {

    private final CountingService countingService;

    private final ObjectMapper objectMapper;

    private final MapUtils mapUtils;

    public NotificationCountingProcessorImpl(
            NotificationService notificationService,
            NotificationSender sender,
            CountingService countingService,
            List<JpaRepository<?, ?>> repositories,
            ObjectMapper objectMapper,
            UserNotificationSettingsService userSettingsService, MapUtils mapUtils,
            TransactionTemplate transactionTemplate) {
        super(repositories, notificationService, sender, objectMapper, userSettingsService, transactionTemplate);
        this.countingService = countingService;
        this.objectMapper = objectMapper;
        this.mapUtils = mapUtils;
    }

    @Override
    protected Iterable<Notification> getNotifications(NotificationService notificationService) {
        return notificationService.getCountingNotSent();
    }

    @Override
    protected Iterable<CountingSettings> getSendSettings(NotificationSettings settings) {
        return countingService.getOfSettings(settings);
    }

    @Override
    @SuppressWarnings("java:S6205")
    protected void doProcess(
            Notification notification, CountingSettings countings
                        ) throws JsonProcessingException {
        log.debug("doProcess: notificationId={}, receiverId={}, entity={}", 
                notification.getId(), notification.getReceiverId(), notification.getEntity());
        var entity = notification.getEntity();
        var settings = notification.getSettings();
        HasContactData receiver;
        if(notification.getCustomer() != null){
            receiver = notification.getCustomer();
        }else {
            receiver = getReceiver(notification.getReceiverId(), settings.getParentType());
        }
        log.debug("doProcess: receiver found, receiver={}", receiver);
        var type = countings.getType();
        var count = countings.getCount();
        var entityObject = objectMapper.readValue(entity, Object.class);
        var initial = mapUtils.extractNode(entityObject, countings.getInitialPropertyName(), Number.class);
        var current = mapUtils.extractNode(entityObject, countings.getPropertyName(), Number.class);
        log.debug("doProcess: type={}, count={}, initial={}, current={}", type, count, initial, current);
        var notificationType = settings.getType();
        switch (type) {
            case EXACT -> {
                if (Objects.equals(current, count)) {
                    log.debug("doProcess: sending notification for EXACT match");
                    send(notification, receiver, notificationType);
                }
            }
            case PERCENT -> {
                var percent = current.doubleValue() / initial.doubleValue();
                if (percent <= count) {
                    log.debug("doProcess: sending notification for PERCENT match, percent={}", percent);
                    send(notification, receiver, notificationType);
                }
            }
            case REMAINS -> {
                var remains = initial.doubleValue() - current.doubleValue();
                if (remains <= count) {
                    log.debug("doProcess: sending notification for REMAINS match, remains={}", remains);
                    send(notification, receiver, notificationType);
                }
            }
        }
        log.debug("doProcess: completed for notificationId={}", notification.getId());
    }
}
