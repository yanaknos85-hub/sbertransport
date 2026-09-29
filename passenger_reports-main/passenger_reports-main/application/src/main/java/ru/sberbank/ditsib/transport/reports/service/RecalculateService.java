package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис пересчета данных по заявках.
 */
public interface RecalculateService {
    
    /**
     * Запустить пересчет.
     *
     * @param statuses статусы.
     * @param from начало интервала.
     * @param to конец интервала.
     * @param authorId идентификаторы авторов.
     */
    void recalculate(List<TripRequestStatus> statuses, LocalDateTime from, LocalDateTime to, List<UUID> authorId);
    
}
