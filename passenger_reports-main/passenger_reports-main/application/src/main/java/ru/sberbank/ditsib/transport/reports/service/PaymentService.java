package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Request;

/**
 * Сервис для работы с платежными данными.
 */
public interface PaymentService {
    
    /**
     * Заполнить платежные данные.
     *
     * @param request исходная заявка.
     * @return
     */
    Request fillPaymentData(Request request);
    
}
