package ru.sber.transport.notifications.services;

import org.springframework.lang.NonNull;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.Map;
import java.util.UUID;

/**
 * Отправление сообщений.
 */
public interface MessageSender {
    
    /**
     * Отправить сообщение.
     *
     * @param id идентификатор сообщения
     * @param receiver получатель.
     * @param notificationType тип уведомления.
     * @param message сообщение.
     * @param data данные сообщения.
     */
    void send(UUID id, HasContactData receiver, NotificationType notificationType, @NonNull String message, Map<String, Object> data);
    
    /**
     * Канал отправки сообщения.
     */
    ChannelType channel();
    
}
