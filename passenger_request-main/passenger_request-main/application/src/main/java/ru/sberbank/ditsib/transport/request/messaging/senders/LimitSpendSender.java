package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;

public interface LimitSpendSender {
    /**
     * Трата лимита.
     *
     * @param request запрос.
     * @param sum сумма запроса.
     * @param isCoop признак совместной поездки
     * @param isDriver признак водителя
     * @param transportType тип транспорта
     * @param moneySaved кол-во сэкономленных средств в копейках
     */
    void spend(
            Request request, Integer sum, boolean isCoop, boolean isDriver, TransportTypeEnum transportType,
            Integer moneySaved
              );
}
