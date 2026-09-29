package ru.sberbank.ditsib.transport.request.service;

import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

public interface InProgressMessageProcessor {
    
    void process(InContractorTaxiTripInProgressMessage message);
    
    TransportTypeEnum transportType();
}
