package ru.sberbank.ditsib.transport.request.service.impl;

import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.service.ApprovalDeadlineCalculator;
import ru.sberbank.ditsib.transport.request.service.ProductionCalendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class PersonalApprovalDeadlineCalculatorImpl implements ApprovalDeadlineCalculator {

    final private ProductionCalendar productionCalendar = new ProductionCalendarImpl();

    /**
     * @param creationTime Дата/время создания заявки
     * @param desiredDate  Желаемая дата/время поездки
     * @return Рассчитанный контрольный срок согласования заявки
     */
    @Override
    public LocalDateTime getApprovalDeadline(LocalDateTime creationTime, LocalDateTime desiredDate, String timeZone) {
        var tmpCreationTime = Optional.ofNullable(creationTime).orElseGet(()->LocalDateTime.now(ZoneId.of(UTC.getId())));
        var approvalDeadline = productionCalendar.addWorkingMinutes(tmpCreationTime, 480, timeZone);
        return approvalDeadline;
    }
    
    @Override
    public LocalDateTime getApprovalDeadline(Request request) {
        var tmpCreationTime = Optional.ofNullable(request.getCreationTime()).orElseGet(()->LocalDateTime.now(ZoneId.of(UTC.getId())));
        var approvalDeadline = productionCalendar.addWorkingMinutes(tmpCreationTime, 480, request.getTimeZone());
        var maxApprovalDeadline = request.getDesiredDate().minusMinutes(15);
        if (request instanceof RequestForPersonal requestForPersonal && requestForPersonal.isCoopTrip() && !requestForPersonal.isSharedRideOwner() &&
            request.getDesiredDate() != null && approvalDeadline != null &&
            approvalDeadline.isAfter(maxApprovalDeadline)) {
            
            // для присоединившегося к совместной поездке на личном транспорте пассажира контрольный срок согласования
            // не может быть позднее чем за 15 минут до начала поездки
            approvalDeadline = maxApprovalDeadline;
        }
        return approvalDeadline;
    }
}
