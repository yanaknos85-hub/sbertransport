package ru.sberbank.ditsib.transport.vehicle.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.impl.OrganizationProviderImpl;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера организаций")
class OrganizationProviderTest {
    
    @InjectMocks
    private OrganizationProviderImpl provider;
    
    @Mock
    private OrganizationService service;
    
    @Mock
    private OrganizationMapper mapper;
    
    private Organization organization;
    
    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .digitId(1L)
                                   .officialName("officialName")
                                   .build();
    }
    
    @Test
    void delete() {
        var message = new OrganizationMessage();
        message.setId(organization.getId());
        message.setOfficialName(organization.getOfficialName());
        message.setDigitId(organization.getDigitId());
        message.setTid("tid");
        message.setMsrn("msrn");
        when(mapper.organizationMessageToOrganization(message)).thenReturn(organization);
        doNothing().when(service).delete(organization);
        provider.delete(message);
        verify(mapper).organizationMessageToOrganization(message);
        verify(service).delete(organization);
    }
    
    @Test
    void save() {
        var message = new OrganizationMessage();
        message.setId(organization.getId());
        message.setOfficialName(organization.getOfficialName());
        message.setDigitId(organization.getDigitId());
        message.setTid("tid");
        message.setMsrn("msrn");
        when(mapper.organizationMessageToOrganization(message)).thenReturn(organization);
        when(service.save(organization)).thenReturn(organization);
        provider.save(message);
        verify(mapper, times(1)).organizationMessageToOrganization(message);
        verify(service, times(1)).save(organization);
    }
}