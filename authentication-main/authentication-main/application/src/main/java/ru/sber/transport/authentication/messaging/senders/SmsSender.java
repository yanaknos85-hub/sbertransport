package ru.sber.transport.authentication.messaging.senders;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * SMS sender.
 */
public interface SmsSender {

    /**
     * Отправить смс.
     *
     * @param phone номер телефона.
     * @param template шаблон.
     */
    void send(String phone, String template, Map<String, Object> data);

}
