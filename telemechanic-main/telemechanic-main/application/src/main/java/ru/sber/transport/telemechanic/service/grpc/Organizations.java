package ru.sber.transport.telemechanic.service.grpc;

import ru.sber.transport.telemechanic.database.model.Organization;

import java.util.UUID;

public interface Organizations {

    Organization one(UUID id);
}
