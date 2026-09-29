package ru.sber.transport.telemechanic.service.grpc;

import ru.sber.transport.telemechanic.database.model.Department;

import java.util.UUID;

public interface Departments {
    
    Department one(UUID id);
}
