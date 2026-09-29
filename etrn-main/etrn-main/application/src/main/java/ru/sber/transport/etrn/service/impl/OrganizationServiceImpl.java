package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.dao.OrganizationRepository;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sber.transport.etrn.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository repository;

    @Override
    public Organization save(Organization organization) {
        return repository.save(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Organization organization) {
        repository.delete(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
}