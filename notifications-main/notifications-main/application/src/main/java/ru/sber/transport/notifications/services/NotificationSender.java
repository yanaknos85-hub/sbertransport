package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.Map;
import java.util.UUID;

/**
 * Отправитель уведомлений.
 */
public interface NotificationSender {
    
    /**
     * Отправка уведомлений.
     *
     * @param id идентификатор уведомления.
     * @param receiver получатель.
     * @param type тип уведомления.
     * @param messages сообщения.
     * @param data данные.
     */
    void send(UUID id, HasContactData receiver, NotificationType type,
              Map<ChannelType, String> messages, Map<String, Object> data);
}
