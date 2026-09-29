package ru.sber.transport.push.business;

import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.SendHistoryDto;

import java.util.Map;
import java.util.UUID;

/**
 * Отправитель пушей.
 */
public interface PushSender {
    
    /**
     * Отправка пуша на устройство.
     *
     * @param sendHistory объект исторических данных.
     * @param token токен.
     * @param type тип уведомления.
     * @param text текст пуша.
     * @param data дополнительные данные к уведомлению.
     *
     * @return идентификатор сообщения.
     */
    String send(SendHistoryDto sendHistory, String token, String type, String text, Map<String, Object> data);

    /**
     * Тип платформы.
     *
     * @return тип платформы.
     */
    PlatformType type();
    
}
