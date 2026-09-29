package ru.sberbank.ditsib.transport.vehicle.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.database.model.Position;
import ru.sberbank.ditsib.transport.vehicle.mapper.PositionMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.impl.PositionProviderImpl;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.PositionService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера должностей")
class PositionProviderTest {
    
    @InjectMocks
    private PositionProviderImpl provider;
    
    @Mock
    private PositionService service;
    
    @Mock
    private PositionMapper mapper;
    
    @Mock
    private OrganizationService organizationService;
    
    private Organization organization;
    private Position position;
    private PositionMessage message;
    
    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .digitId(1L)
                                   .officialName("officialName")
                                   .build();
        position = Position.builder()
                           .id(UUID.randomUUID())
                           .organization(organization)
                           .positionName("positionName")
                           .build();
        message = PositionMessage.builder()
                                 .id(position.getId())
                                 .positionName(position.getPositionName())
                                 .organizationId(organization.getId())
                                 .selfApproved(true)
                                 .build();
    }
    
    @Test
    void delete() {
        when(mapper.positionMessageToPosition(message)).thenReturn(position);
        doNothing().when(service).delete(position);
        provider.delete(message);
        verify(mapper).positionMessageToPosition(message);
        verify(service).delete(position);
    }
    
    @Test
    void save() {
        when(mapper.positionMessageToPosition(message)).thenReturn(position);
        when(service.save(position)).thenReturn(position);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        provider.save(message);
        verify(mapper, times(1)).positionMessageToPosition(message);
        verify(service, times(1)).save(position);
        verify(organizationService, times(1)).get(message.getOrganizationId());
    }
    
    @Test
    void saveNoOrganization() {
        when(mapper.positionMessageToPosition(message)).thenReturn(position.toBuilder().organization(null).build());
        message.setOrganizationId(null);
        provider.save(message);
        verify(mapper, times(1)).positionMessageToPosition(message);
        verify(service, times(0)).save(position);
        verify(organizationService, times(0)).get(message.getOrganizationId());
    }
    
    @Test
    void saveNoOrganizationInDatabase() {
        when(mapper.positionMessageToPosition(message)).thenReturn(position);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(EntityNotFoundException.class);
        verify(mapper, times(1)).positionMessageToPosition(message);
        verify(service, times(0)).save(position);
        verify(organizationService, times(1)).get(message.getOrganizationId());
    }
}