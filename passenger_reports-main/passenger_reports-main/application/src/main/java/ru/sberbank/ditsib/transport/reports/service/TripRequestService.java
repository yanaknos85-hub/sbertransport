package ru.sberbank.ditsib.transport.reports.service;

import ru.sber.transport.request.messaging.RequestMessage;

public interface TripRequestService {
    
    void processMessage(RequestMessage message);
}
