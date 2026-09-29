package ru.sberbank.ditsib.transport.reports.messaging.senders;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateRequestStatusFromReportsSender {
    /**
     * Отправить.
     *
     * @param requestId идентификатор заявки.
     * @param requestStatus статус для отправки.
     * @param dateTime дата и время изменения статуса. Используется для фиксации времени смены статуса
     * для последующей обработки контрольных сроков.
     */
    void send(UUID requestId, String requestStatus, LocalDateTime dateTime, UUID userId);
}
