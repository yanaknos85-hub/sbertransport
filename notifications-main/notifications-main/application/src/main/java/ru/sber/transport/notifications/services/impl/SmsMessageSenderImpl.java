package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasPhone;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
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
class SmsMessageSenderImpl implements MessageSender {

    private final SmsSender sender;

    @Override
    public void send(UUID id, HasContactData receiver, NotificationType notificationType, @NonNull String message,
                     Map<String, Object> data) {
        if (receiver instanceof HasPhone hasPhone && hasPhone.getPhone() != null && hasPhone.isPhoneConfirmed()) {
            sender.send(List.of(hasPhone.getPhone()), message, data);
        }
    }

    @Override
    public ChannelType channel() {
        return ChannelType.SMS;
    }
}
