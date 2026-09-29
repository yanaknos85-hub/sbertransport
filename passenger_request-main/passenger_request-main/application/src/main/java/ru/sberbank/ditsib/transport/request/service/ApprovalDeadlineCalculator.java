package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.Request;

import java.time.LocalDateTime;

/**
 * Калькулятор контрольного срока согласования заявки
 */
public interface ApprovalDeadlineCalculator {
    /**
     * @param creationTime Дата/время создания заявки
     * @param desiredDate  Желаемая дата/время поездки
     *
     * @return Рассчитанный контрольный срок согласования заявки
     */
    LocalDateTime getApprovalDeadline(LocalDateTime creationTime, LocalDateTime desiredDate, String timeZone);
    
    /**
     * @param request заявка
     *
     * @return рассчитанный контрольный срок согласования заявки
     */
    default LocalDateTime getApprovalDeadline(Request request) {
        return request.getApprovalDeadline();
    }
}
