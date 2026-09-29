package ru.sberbank.ditsib.transport.request.service.impl;

import ru.sberbank.ditsib.transport.request.service.PaymentDoneDeadlineCalculator;
import ru.sberbank.ditsib.transport.request.service.ProductionCalendar;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class PersonalPaymentDoneDeadlineCalculatorImpl implements PaymentDoneDeadlineCalculator {
    final private ProductionCalendar productionCalendar = new ProductionCalendarImpl();
    /**
     * @param orderPaymentFormationStartDate Дата/время готовности заявки для формирования приказа на выплату
     * @param timeZone                       Тайм-зона заявки
     * @return Рассчитанный контрольный срок выплаты компенсации по заявке
     */
    @Override
    public LocalDateTime getPaymentDoneDeadline(LocalDateTime orderPaymentFormationStartDate, String timeZone) {
        var tmpCreationTime = Optional.ofNullable(orderPaymentFormationStartDate).orElseGet(()->LocalDateTime.now(ZoneId.of(UTC.getId())));
        var approvalDeadline = productionCalendar.addWorkingMinutes(tmpCreationTime, 5790, timeZone); // 5790 -> 12 рабочих дней
        return approvalDeadline;
    }
}
