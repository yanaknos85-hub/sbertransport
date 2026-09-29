package ru.sberbank.ditsib.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of service for working with organization.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository repository;

    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Organization entity) {
        repository.save(entity.withActive(false));
    }

    @Override
    public Organization save(Organization entity) {
        return repository.save(entity);
    }

}
