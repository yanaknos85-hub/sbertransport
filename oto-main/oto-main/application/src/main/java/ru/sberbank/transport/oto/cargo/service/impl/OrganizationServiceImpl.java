package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationRepository;
import ru.sberbank.transport.oto.cargo.database.model.Organization;
import ru.sberbank.transport.oto.cargo.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository repository;
    @Override
    public Optional<Organization> findById(UUID uuid) {
        return repository.findById(uuid);
    }

    @Override
    public Organization save(Organization organization) {
        return repository.save(organization);
    }

    @Override
    public Organization findOrCreateById(UUID uuid) {
        return repository.findById(uuid)
                         .orElseGet(() -> repository.save(new Organization(uuid)));
    }

    @Override
    public void delete(Organization organization) {
        repository.delete(organization);
    }
}
