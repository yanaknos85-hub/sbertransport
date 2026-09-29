package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.Request;

public interface TariffGrpcClient {
    
    void recalculate(Request request);
    
}
