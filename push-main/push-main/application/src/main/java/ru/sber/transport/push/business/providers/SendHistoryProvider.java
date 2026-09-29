package ru.sber.transport.push.business.providers;

import ru.sber.transport.push.business.dto.SendHistoryDto;

/**
 * Провайдер исторических данных отправленных сообщений.
 */
public interface SendHistoryProvider {

    /**
     * Сохранение исторических данных отправки уведомлений
     * @param sendHistoryDto исторические данные
     */
    void save(SendHistoryDto sendHistoryDto);

}
