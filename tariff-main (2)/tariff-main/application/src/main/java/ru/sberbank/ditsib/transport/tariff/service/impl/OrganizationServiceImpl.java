package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.service.OrganizationService;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
    public Optional<Organization> get(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public void delete(Organization organization) {
        organization.setActive(false);
        repository.save(organization);
    }
    
    @Override
    public void save(Organization organization) {
        repository.save(organization);
    }

    @Override
    public Map<UUID, String> getNames(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return repository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));
    }
}
