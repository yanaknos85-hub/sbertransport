package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;

public interface EdfOperatorClientService {
    
    KorusEwbTitleResponse sendTitle(String employeeFullName, String fileName, byte[] content, String signature);
}
