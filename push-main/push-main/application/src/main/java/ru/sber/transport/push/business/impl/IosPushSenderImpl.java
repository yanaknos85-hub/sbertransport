package ru.sber.transport.push.business.impl;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.providers.SendHistoryProvider;

/**
 * Реализация отправителя пушей ANDROID.
 */
@Component
@Slf4j
class IosPushSenderImpl extends FirebaseSender implements PushSender {

    public IosPushSenderImpl(FirebaseMessaging messaging, SendHistoryProvider sendHistoryProvider) {
        super(messaging, sendHistoryProvider);
    }

    @Override
    public PlatformType type() {
        return PlatformType.IOS;
    }

    @Override
    protected Message.Builder customizeBody(Message.Builder builder, String text) {
        return builder.setNotification(Notification.builder().setBody(text).build());
    }
}
