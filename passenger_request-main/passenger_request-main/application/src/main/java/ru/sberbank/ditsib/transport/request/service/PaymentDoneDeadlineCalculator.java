package ru.sberbank.ditsib.transport.request.service;

import java.time.LocalDateTime;

public interface PaymentDoneDeadlineCalculator {
    /**
     * @param orderPaymentFormationStartDate Дата/время готовности заявки для формирования приказа на выплату
     * @param timeZone  Тайм-зона заявки
     *
     * @return Рассчитанный контрольный срок выплаты компенсации по заявке
     */
    LocalDateTime getPaymentDoneDeadline(LocalDateTime orderPaymentFormationStartDate, String timeZone);
}
