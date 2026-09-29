package ru.sber.transport.authsb.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authsb.database.dao.OrganizationRepository;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.services.OrganizationService;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    public Optional<Organization> findByOgrnAndKpp(String ogrn, String kpp) {
        return organizationRepository.findByOgrnAndKpp(ogrn, kpp);
    }

    @Override
    @Transactional
    public Organization save(Organization organization) {
        return organizationRepository.save(organization);
    }
}