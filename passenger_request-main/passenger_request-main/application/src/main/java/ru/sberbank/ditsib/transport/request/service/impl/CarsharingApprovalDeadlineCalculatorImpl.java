package ru.sberbank.ditsib.transport.request.service.impl;

import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;
import ru.sberbank.ditsib.transport.request.service.ProductionCalendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class CarsharingApprovalDeadlineCalculatorImpl implements ApprovalDeadlineCalculator {
    
    final private ProductionCalendar productionCalendar = new ProductionCalendarImpl();
    
    /**
     * @param creationTime Дата/время создания заявки
     * @param desiredDate  Желаемая дата/время поездки
     * @return Рассчитанный контрольный срок согласования заявки
     */
    @Override
    public LocalDateTime getApprovalDeadline(LocalDateTime creationTime, LocalDateTime desiredDate, String timeZone) {
        var tmpCreationTime = Optional.ofNullable(creationTime).orElseGet(()->LocalDateTime.now(ZoneId.of(UTC.getId())));
        return productionCalendar.addWorkingMinutes(tmpCreationTime, 480, timeZone);
    }
}
