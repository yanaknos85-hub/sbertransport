package ru.sber.transport.push.business;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для работы с PUSH.
 */
public interface PushService {
    
    /**
     * Отправить уведомление.
     *
     * @param messageId идентификатор сообщения.
     * @param recipients получатели.
     * @param type тип данных.
     * @param text текст.
     * @param data доп. данные.
     */
    void send(UUID messageId, List<UUID> recipients, String type, String text, Map<String, Object> data);
    
}
