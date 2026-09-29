package com.google.firebase.messaging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.sberbank.utils.reflection.ReflectionUtils;

@RequiredArgsConstructor
public class NotificationTestUtil {
    
    private final Notification notification;
    
    @SneakyThrows
    public String getBody() {
        var notificationClass = notification.getClass();
        var bodyField = ReflectionUtils.getField(notificationClass, "body");
        bodyField.setAccessible(true);
        var rawBody = bodyField.get(notification);
        return String.valueOf(rawBody);
    }
    
}
