package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sber.transport.telemechanic.mapper.OrganizationMapper;
import ru.sber.transport.telemechanic.provider.OrganizationProvider;
import ru.sber.transport.telemechanic.service.OrganizationContactService;
import ru.sber.transport.telemechanic.service.OrganizationGroupService;
import ru.sber.transport.telemechanic.service.OrganizationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Реализация провайдера организаций.
 */
@Transactional
@RequiredArgsConstructor
@Component
public class OrganizationProviderImpl implements OrganizationProvider {
    
    private final OrganizationService organizationService;
    private final OrganizationGroupService organizationGroupService;
    private final OrganizationContactService organizationContractService;
    private final OrganizationMapper mapper;
    
    @Override
    public void delete(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.delete(entity);
        organizationContractService.deleteAll(entity.getId());
    }
    
    @Override
    public void save(OrganizationMessage message) {
        if (message.getOrganizationGroup() != null) {
            organizationGroupService.save(message.getOrganizationGroup());
        }
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.save(entity);
    }
    
    @Override
    public void addContractor(ContractorMessage contractorMessage) {
        var organizationOpt = organizationService.get(contractorMessage.organizations().getFirst());
        organizationOpt.ifPresent(organization -> {
            organization.setContractorExternalId(contractorMessage.externalId());
            organizationService.save(organization);
        });
    }
    
    @Override
    public void deleteContractor(ContractorMessage contractorMessage) {
        var organizationOpt = organizationService.get(contractorMessage.organizations().getFirst());
        organizationOpt.ifPresent(organization -> {
            if(organization.getContractorExternalId().equals(contractorMessage.externalId())) {
                organization.setContractorExternalId(null);
                organizationService.save(organization);
            }
        });
    }
}
