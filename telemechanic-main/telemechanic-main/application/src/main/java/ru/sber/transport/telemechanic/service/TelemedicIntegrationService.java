package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.Ewb;

public interface TelemedicIntegrationService {
    
    void sendTitleToContractor(String name, String content, Ewb ewb, String contractorApiToken);
}
