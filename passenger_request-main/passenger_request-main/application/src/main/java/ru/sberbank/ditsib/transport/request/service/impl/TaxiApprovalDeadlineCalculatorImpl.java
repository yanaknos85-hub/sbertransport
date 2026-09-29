package ru.sberbank.ditsib.transport.request.service.impl;

import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class TaxiApprovalDeadlineCalculatorImpl implements ApprovalDeadlineCalculator {
    /**
     * @param creationTime Дата/время создания заявки
     * @param desiredDate Желаемая дата/время поездки
     *
     * @return Рассчитанный контрольный срок согласования заявки
     */
    @Override
    public LocalDateTime getApprovalDeadline(LocalDateTime creationTime, LocalDateTime desiredDate, String timeZone) {
        var tmpCreationTime = Optional.ofNullable(creationTime).orElseGet(()->LocalDateTime.now(ZoneId.of(UTC.getId())));
        var approvalDeadline = tmpCreationTime.plusMinutes(15);
        if (desiredDate != null && !tmpCreationTime.plusHours(2).isAfter(desiredDate)) {
            approvalDeadline = desiredDate.minusHours(1);
        };
        return approvalDeadline;
    }
}
