package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
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
@Slf4j
@RequiredArgsConstructor
@Setter
class EmailMessageSenderImpl implements MessageSender {
    
    private final EmailSender sender;
    
    @Override
    public void send(UUID id, HasContactData receiver, NotificationType notificationType, @NonNull String message,
                     Map<String, Object> data) {
        if (receiver instanceof HasEmail hasEmail && hasEmail.getEmail() != null) {
            sender.send(List.of(hasEmail.getEmail()), null, message, data);
        }
    }
    
    @Override
    public ChannelType channel() {
        return ChannelType.EMAIL;
    }
}
