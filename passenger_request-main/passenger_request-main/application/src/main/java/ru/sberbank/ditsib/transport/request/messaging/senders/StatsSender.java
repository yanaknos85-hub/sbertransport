package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sber.transport.request.model.StatsDTO;

/**
 * Отправитель дополнительных данных для заявок по такси.
 */
public interface StatsSender {
    
    /**
     * Отправить.
     *
     * @param statsDTO данные для отправки.
     */
    void send(StatsDTO statsDTO);
}
