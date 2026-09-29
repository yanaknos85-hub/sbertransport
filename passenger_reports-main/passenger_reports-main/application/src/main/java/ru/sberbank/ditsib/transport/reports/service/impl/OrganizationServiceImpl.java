package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.reports.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.reports.model.Organization;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;

import jakarta.validation.constraints.NotNull;
import java.util.List;
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
    
    @Override
    public List<Organization> findByOrganizationGroupId(UUID id) {
       return repository.findAllByOrganizationGroupId(id);
    }
    
    @Override
    public List<Organization> findOrganizationsBySearchParameter(String searchParameter) {
       return repository.findAllByOfficialNameContainingIgnoreCase(searchParameter);
    }
    
    @Override
    public List<Organization> findAll() {
        return repository.findAll();
    }
}
