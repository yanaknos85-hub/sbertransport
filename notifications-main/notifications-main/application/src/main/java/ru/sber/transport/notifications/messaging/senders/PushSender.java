package ru.sber.transport.notifications.messaging.senders;

import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Отправитель PUSH.
 */
public interface PushSender {
    
    /**
     * Отправить PUSH.
     *
     * @param id идентификатор уведомления.
     * @param receivers получатели.
     * @param type тип уведомления.
     * @param template шаблон текста.
     * @param data данные.
     * @param additionalData дополнительные данные.
     */
    void send(UUID id, List<UUID> receivers, NotificationType type, String template, Map<String, Object> data,
              Map<String, Object> additionalData);
    
}
