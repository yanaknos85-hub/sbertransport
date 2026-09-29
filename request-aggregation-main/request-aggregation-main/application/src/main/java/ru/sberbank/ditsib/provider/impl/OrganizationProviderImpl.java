package ru.sberbank.ditsib.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.mappers.OrganizationMapper;
import ru.sberbank.ditsib.provider.OrganizationProvider;
import ru.sberbank.ditsib.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Реализация провайдера организаций.
 */
@Transactional
@RequiredArgsConstructor
@Component
public class OrganizationProviderImpl implements OrganizationProvider {

    private final OrganizationService organizationService;
    private final OrganizationMapper mapper;

    @Override
    public void delete(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.delete(entity);
    }

    @Override
    public void save(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.save(entity);
    }
}
