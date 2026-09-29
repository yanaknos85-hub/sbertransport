package ru.sberbank.ditsib.transport.request.messaging.senders;


import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;

/**
 * Отправитель сообщений контрагенту.
 */
public interface ContractorMessageSender {
    
    /**
     * Отправить информацию о поездке на такси.
     *
     * @param tripDTO данные о поездке.
     */
    void send(OutContractorTaxiTripMessage tripDTO);
    
}
