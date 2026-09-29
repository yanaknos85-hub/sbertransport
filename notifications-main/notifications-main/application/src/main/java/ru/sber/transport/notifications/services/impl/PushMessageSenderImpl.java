package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasId;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация сервиса отправки PUSH.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class PushMessageSenderImpl implements MessageSender {
    
    private final PushSender pushSender;
    
    @Override
    public void send(UUID id, HasContactData receiver, NotificationType notificationType, @NonNull String message,
                     Map<String, Object> data) {
        if (receiver instanceof HasId hasId && hasId.getId() != null) {
            pushSender.send(id, List.of(hasId.getId()), notificationType, message, null, data);
        }
    }
    
    @Override
    public ChannelType channel() {
        return ChannelType.PUSH;
    }
}
