package ru.sber.transport.notifications.messaging.senders;

import java.util.List;
import java.util.Map;

/**
 * Отправитель электронной почты.
 */
public interface EmailSender {
    
    /**
     * Отправить почту.
     *
     * @param recipients получатели.
     * @param subject тема.
     * @param template шаблон текста.
     * @param data данные.
     */
    void send(List<String> recipients, String subject, String template, Map<String, Object> data);
    
}
