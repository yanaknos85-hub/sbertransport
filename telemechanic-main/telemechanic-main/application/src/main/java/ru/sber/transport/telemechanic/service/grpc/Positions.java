package ru.sber.transport.telemechanic.service.grpc;

import ru.sber.transport.telemechanic.database.model.Position;

import java.util.UUID;

public interface Positions {
    
    Position one(UUID id);
}
