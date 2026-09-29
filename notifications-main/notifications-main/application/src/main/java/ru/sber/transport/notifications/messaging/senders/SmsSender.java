package ru.sber.transport.notifications.messaging.senders;

import java.util.List;
import java.util.Map;

/**
 * Отправитель SMS.
 */
public interface SmsSender {
    
    /**
     * Отправить SMS.
     *
     * @param phones получатели.
     * @param template шаблон текста.
     * @param data данные.
     */
    void send(List<String> phones, String template, Map<String, Object> data);
    
}
