package ru.sberbank.ditsib.transport.request.service.corp.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of service for working with organization.
 */
@RequiredArgsConstructor
@Service
@Transactional
class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    
    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public List<Organization> getAll() {
        return repository.findAllByActive(true);
    }
    
    @Override
    public void check(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(Organization.class, id);
        }
    }
    
    @Override
    public Organization getOrganization(UUID id) {
        return get(id).orElseThrow(() -> new EntityNotFoundException(Organization.class, id));
    }
    
    @Override
    public void delete(Organization organization) {
        organization.setActive(false);
        repository.save(organization);
    }
    
    @Override
    public Organization save(Organization organization) {
        return repository.save(organization);
    }
}
