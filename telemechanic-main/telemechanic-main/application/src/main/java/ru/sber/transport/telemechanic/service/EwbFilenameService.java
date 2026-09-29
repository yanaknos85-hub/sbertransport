package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.common.EwbTitleType;

import java.util.UUID;

public interface EwbFilenameService {
    
    String generateFilename(EwbTitleType titleType, UUID organizationId, UUID departmentId);
}
