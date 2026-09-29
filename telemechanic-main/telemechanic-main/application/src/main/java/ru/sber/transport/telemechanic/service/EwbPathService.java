package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.Employee;

public interface EwbPathService {
    
    boolean calculatingClientPath(Employee driver);
}
