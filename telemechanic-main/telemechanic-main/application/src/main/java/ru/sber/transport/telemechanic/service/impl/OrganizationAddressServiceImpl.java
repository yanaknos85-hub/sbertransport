package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.dao.OrganizationAddressRepository;
import ru.sber.transport.telemechanic.database.model.OrganizationAddress;
import ru.sber.transport.telemechanic.exception.OrganizationAddressNotFoundException;
import ru.sber.transport.telemechanic.service.OrganizationAddressService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationAddressServiceImpl implements OrganizationAddressService {
    
    private final OrganizationAddressRepository repository;
    
    @Override
    public OrganizationAddress get(UUID organizationId) {
        return repository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new OrganizationAddressNotFoundException(organizationId));
    }
}
