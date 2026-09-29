package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;
import ru.sber.transport.notifications.services.NotificationSender;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
@Transactional
class NotificationSenderImpl implements NotificationSender {

    private final List<MessageSender> senders;

    @Override
    public void send(UUID id, HasContactData receiver, NotificationType type,
                     Map<ChannelType, String> messages, Map<String, Object> data) {
        for (var sender : senders) {
            var channel = sender.channel();
            if (messages.containsKey(channel) && channel.getContactClass().isAssignableFrom(receiver.getClass())) {
                var message = messages.get(channel);
                sender.send(id, receiver, type, message, data);
            }
        }
    }
}
