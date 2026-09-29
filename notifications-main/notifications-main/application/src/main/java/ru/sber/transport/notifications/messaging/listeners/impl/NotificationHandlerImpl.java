package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.messaging.listeners.NotificationHandler;
import ru.sber.transport.notifications.messaging.message.NotificationMessage;
import ru.sber.transport.notifications.services.NotificationService;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Класс-слушатель сообщений об уведомлениях.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationHandlerImpl implements NotificationHandler {

    private final NotificationService notificationService;
    private final NotificationSettingsRepository settingsRepository;
    private final ObjectMapper mapper;

    @Override
    @Transactional
    public void accept(NotificationMessage message) {
        var data = mapDataList(message);
        for (var receiver : message.receivers()) {
            var settings = settingsRepository.findAllByReceiverIdAndNotificationType(receiver, message.messageType());

            if (!settings.isEmpty()) {
                saveNotification(message.getId(), receiver, settings.getFirst(), data);
            }
        }
    }

    private String mapDataList(NotificationMessage message) {
        var data = message.data()
                .stream()
                .collect(Collectors.toMap(NotificationMessage.Data::key, NotificationMessage.Data::value));
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при чтении данных из сообщения id {}", message.getId(),e);
            throw new IllegalArgumentException(e);
        }
    }

    private void saveNotification(UUID id, UUID receiver, NotificationSettings settings,
                                  String entity) {
        var notification = new Notification();
        notification.setSettings(settings);
        notification.setReceiverId(receiver);
        notification.setEntityId(id);
        notification.setSent(false);
        notification.setEntity(entity);
        notificationService.save(notification);
        log.debug("Notification saved: {}", entity);
    }
}