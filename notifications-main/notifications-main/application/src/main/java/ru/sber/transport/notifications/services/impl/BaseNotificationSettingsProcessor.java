package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.SendSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationSender;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@EnableScheduling
abstract class BaseNotificationSettingsProcessor<T extends SendSettings> implements NotificationProcessor {

    private final List<JpaRepository<?, ?>> repositories;

    private final NotificationService notificationService;

    private final NotificationSender sender;

    private final ObjectMapper objectMapper;

    private final UserNotificationSettingsService userSettingsService;

    private final TransactionTemplate transactionTemplate;


    @Override
    @Transactional
    @Scheduled(cron = "${notification.settings.schedule}")
    public void process() {
        getNotifications(notificationService).forEach(notification -> {
            try {
                transactionTemplate.executeWithoutResult(status ->
                    process(notification)
                );
            } catch (Exception e) {
                log.error("Processing upper notification failed", e);
                notification.setSentError(true);
                notificationService.save(notification);
            }
        });
    }

    @Transactional
    @Override
    public void process(Notification notification) {
        var settings = notification.getSettings();
        getSendSettings(settings).forEach(sendSettings -> {
            try {
                doProcess(notification, sendSettings);
            } catch (JsonProcessingException e) {
                log.error("Processing failed", e);
            }
        });
    }

    protected void send(Notification notification, HasContactData receiver, NotificationType notificationType) throws JsonProcessingException {
        log.debug("send: notificationId={}, receiver={}, notificationType={}", 
                notification.getId(), receiver, notificationType);
        var channels = notification.getSettings().getChannels();
        log.debug("BaseNotificationSettingsProcessor.send: {}", notification.getSettings().getDescription());
        var entityObject = objectMapper.readValue(notification.getEntity(), new TypeReference<Map<String, Object>>() {
        });
        var messages = channels.stream()
                .filter(ChannelSettings::isActive)
                .collect(Collectors.toMap(ChannelSettings::getChannel,
                        ChannelSettings::getText, (l, r) -> l));
        userSettingsService.updateChannelMap(receiver, messages, notification.getSettings().getId());
        if (!messages.isEmpty()){
            sender.send(notification.getId(), receiver, notificationType, messages, entityObject);
        }
        notification.setSent(true);
        notificationService.save(ReflectionUtils.cast(notification));
        log.debug("send: completed, notificationId={}, sent=true", notification.getId());
    }

    protected void setSentWithoutSendingMessage(Notification notification) {
        notification.setSent(true);
        notificationService.save(ReflectionUtils.cast(notification));
    }

    protected abstract void doProcess(Notification notification, T settings) throws JsonProcessingException;

    protected abstract Iterable<T> getSendSettings(NotificationSettings settings);

    protected abstract Iterable<Notification> getNotifications(NotificationService notificationService);

    protected HasContactData getReceiver(UUID receiverId, NotificationSettings.ParentType parentType) {
        var receiverClass = parentType.getReceiverClass();
        var repository = getRepository(receiverClass);
        return repository.findById(receiverId).orElseThrow(() -> new EntityNotFoundException(receiverClass, receiverId));
    }

    private <R extends HasContactData> JpaRepository<R, UUID> getRepository(Class<R> receiverClass) {
        var type = ResolvableType.forClassWithGenerics(JpaRepository.class, receiverClass, UUID.class);
        var repository = repositories.stream().filter(type::isInstance).findFirst().orElseThrow(() -> new NoSuchBeanDefinitionException("Repository for %s not found".formatted(receiverClass.getCanonicalName())));
        return ReflectionUtils.cast(repository);
    }

}
