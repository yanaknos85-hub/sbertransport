package ru.sber.transport.telemechanic.service;

import java.util.UUID;

public interface TinService {
    
    void saveOrUpdate(UUID employeeId, String tin);
}
