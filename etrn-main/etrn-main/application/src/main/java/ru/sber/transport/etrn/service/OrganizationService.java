package ru.sber.transport.etrn.service;

import ru.sber.transport.etrn.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    Organization save(Organization organization);

    void delete(Organization organization);

    Optional<Organization> findById(UUID id);

    Optional<Organization> get(UUID id);
}