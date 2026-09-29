package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiTripMessage;

/**
 * Отправитель дополнительных данных для заявок по такси.
 */
public interface TaxiTripSender {
    
    void send(TaxiTripMessage taxiTripMessage);
    
    /**
     * Отправить.
     *
     * @param taxiTrip данные для отправки.
     */
    void send(CoopTaxiTrip taxiTrip);
    void send(SingleTaxiTrip taxiTrip);

}
