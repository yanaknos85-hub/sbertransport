package ru.sber.transport.telemechanic.provider.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.telemechanic.provider.impl.OrganizationProviderImpl;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.mapper.OrganizationMapper;
import ru.sber.transport.telemechanic.service.OrganizationContactService;
import ru.sber.transport.telemechanic.service.OrganizationGroupService;
import ru.sber.transport.telemechanic.service.OrganizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера организаций")
class OrganizationProviderImplTest {
    
    @InjectMocks
    private OrganizationProviderImpl provider;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationGroupService organizationGroupService;
    @Mock
    private OrganizationContactService organizationContactService;
    @Mock
    private OrganizationMapper mapper;
    
    @Test
    void delete() {
        var message = Instancio.create(OrganizationMessage.class);
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).organizationMessageToOrganization(message);
        doNothing().when(organizationService).delete(entity);
        doNothing().when(organizationContactService).deleteAll(entity.getId());
        provider.delete(message);
        verify(mapper).organizationMessageToOrganization(any(OrganizationMessage.class));
        verify(organizationService).delete(any(Organization.class));
        verify(organizationContactService).deleteAll(any(UUID.class));
    }
    
    @Test
    void save() {
        var contact1 = Instancio.create(ContactMessage.class);
        var contact2 = Instancio.create(ContactMessage.class);
        var message = Instancio.create(OrganizationMessage.class);
        message.setContacts(List.of(contact1, contact2));
        var entity = Instancio.create(Organization.class);
        doNothing().when(organizationGroupService).save(message.getOrganizationGroup());
        doReturn(entity).when(mapper).organizationMessageToOrganization(message);
        doReturn(entity).when(organizationService).save(entity);
        provider.save(message);
        verify(organizationGroupService).save(any(OrganizationMessage.OrganizationGroup.class));
        verify(mapper).organizationMessageToOrganization(any(OrganizationMessage.class));
        verify(organizationService).save(any(Organization.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void addContractor(boolean organizationExists) {
        var organizationId = UUID.randomUUID();
        var externalId = UUID.randomUUID();
        var contractorMessage = Instancio.of(ContractorMessage.class)
                .set(field(ContractorMessage::externalId), externalId)
                .set(field(ContractorMessage::organizations), new ArrayList<>(List.of(organizationId)))
                .create();
        
        var organization = new Organization();
        organization.setId(organizationId);
        organization.setContractorExternalId(null);
        
        if (organizationExists) {
            when(organizationService.get(organizationId)).thenReturn(Optional.of(organization));
            when(organizationService.save(any(Organization.class))).thenReturn(organization);
        } else {
            when(organizationService.get(organizationId)).thenReturn(Optional.empty());
        }
        
        provider.addContractor(contractorMessage);
        verify(organizationService).get(organizationId);
        
        if (organizationExists) {
            verify(organizationService).save(any(Organization.class));
            Assertions.assertEquals(organization.getContractorExternalId(), externalId);
        } else {
            verify(organizationService, never()).save(any(Organization.class));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "true, false",
        "false, false"
    })
    void deleteContractor(boolean organizationExists, boolean externalIdMatches) {
        var organizationId = UUID.randomUUID();
        var messageExternalId = UUID.randomUUID();
        var organizationExternalId = externalIdMatches ? messageExternalId : UUID.randomUUID();
        
        var contractorMessage = Instancio.of(ContractorMessage.class)
                .set(field(ContractorMessage::externalId), messageExternalId)
                .set(field(ContractorMessage::organizations), new ArrayList<>(List.of(organizationId)))
                .create();
        
        var organization = new Organization();
        organization.setId(organizationId);
        organization.setContractorExternalId(organizationExternalId);
        
        if (organizationExists) {
            when(organizationService.get(organizationId)).thenReturn(Optional.of(organization));
            if(externalIdMatches) {
                when(organizationService.save(any(Organization.class))).thenReturn(organization);
            }
        } else {
            when(organizationService.get(organizationId)).thenReturn(Optional.empty());
        }
        
        provider.deleteContractor(contractorMessage);
        verify(organizationService).get(organizationId);
        
        if (organizationExists && externalIdMatches) {
            verify(organizationService).save(any(Organization.class));
            Assertions.assertNull(organization.getContractorExternalId());
        } else {
            verify(organizationService, never()).save(any(Organization.class));
        }
    }
}