package ru.sber.transport.telemechanic.provider.impl;

import ch.qos.logback.classic.Level;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.mapper.FleetOwnerOrganizationMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;
import ru.sber.transport.telemechanic.provider.impl.FleetOwnerOrganizationProviderImpl;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;
import ru.sber.transport.telemechanic.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetOwnerOrganizationProviderImplTest {
    @InjectMocks
    private FleetOwnerOrganizationProviderImpl provider;
    @Mock
    private FleetOwnerOrganizationService fleetOwnerOrganizationService;
    @Mock
    private FleetOwnerOrganizationMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(FleetOwnerOrganizationProviderImpl.class);
    
    @Test
    void save() {
        var organization = Instancio.create(Organization.class);
        var entity = Instancio.create(FleetOwnerOrganization.class);
        var message1 = Instancio.create(FleetOwnerOrganizationMessage.class);
        var message2 = Instancio.create(FleetOwnerOrganizationMessage.class);
        doReturn(Optional.of(organization)).when(organizationService).get(message1.organizationId());
        doReturn(Optional.empty()).when(organizationService).get(message2.organizationId());
        doReturn(entity).when(mapper).fleetOwnerOrganizationMessageToFleetOwnerOrganization(message1,
                                                                                            organization,
                                                                                            true);
        doReturn(entity).when(fleetOwnerOrganizationService).save(entity);
        provider.save(message1);
        assertThatThrownBy(() -> provider.save(message2))
                .isInstanceOf(AwaitingSynchronizationException.class)
                .hasMessage("Awaiting an organization synchronization");
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        check(message2.id(), message2.organizationId());
    }
    
    private void check(UUID messageId, UUID organizationId) {
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        assertEquals(FleetOwnerOrganizationProviderImpl.class.getName(), firstEvent.getLoggerName());
        assertEquals("Can't save fleet owner organization, id:%s, organizationId:%s, awaiting organization synchronization"
                             .formatted(messageId, organizationId),
                     firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        verify(organizationService, times(2)).get(any(UUID.class));
        verify(mapper).fleetOwnerOrganizationMessageToFleetOwnerOrganization(any(FleetOwnerOrganizationMessage.class),
                                                                             any(Organization.class),
                                                                             anyBoolean());
        verify(fleetOwnerOrganizationService).save(any(FleetOwnerOrganization.class));
    }
}