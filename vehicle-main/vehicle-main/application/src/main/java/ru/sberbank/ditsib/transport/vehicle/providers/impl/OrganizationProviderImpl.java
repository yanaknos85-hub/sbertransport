package ru.sberbank.ditsib.transport.vehicle.providers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.OrganizationProvider;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

/**
 * Реализация провайдера организаций.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class OrganizationProviderImpl implements OrganizationProvider {
    
    private final OrganizationService service;
    
    private final OrganizationMapper mapper;
    
    @Override
    public void delete(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        service.delete(entity);
    }
    
    @Override
    public void save(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        service.save(entity);
    }
}
