package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.OrganizationAddress;

import java.util.UUID;

public interface OrganizationAddressService {
    
    OrganizationAddress get(UUID organizationId);
}
