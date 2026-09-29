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
class AndroidPushSenderImpl extends FirebaseSender implements PushSender {

    public AndroidPushSenderImpl(FirebaseMessaging messaging, SendHistoryProvider sendHistoryProvider) {
        super(messaging, sendHistoryProvider);
    }

    @Override
    public PlatformType type() {
        return PlatformType.ANDROID;
    }

    @Override
    protected Message.Builder customizeBody(Message.Builder builder, String text) {
        return builder.putData("body", text);
    }
}
