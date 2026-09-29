package ru.sber.transport.authentication.messaging.senders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Отправитель электронной почты.
 */
public interface EmailSender {
    
    /**
     * Отправить.
     *
     * @param emails получатели.
     * @param subject тема.
     * @param template шаблон.
     * @param data данные.
     */
    void send(List<String> emails, String subject, String template, Map<String, Object> data);
    
}
