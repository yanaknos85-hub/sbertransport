package ru.sber.transport.telemechanic.provider.impl;

import ch.qos.logback.classic.Level;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.mapper.PositionMapper;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера должностей")
class PositionProviderImplTest {
    
    @InjectMocks
    private PositionProviderImpl provider;
    @Mock
    private PositionService service;
    @Mock
    private PositionMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @Captor
    private ArgumentCaptor<Position> positionArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(PositionProviderImpl.class);
    
    @Test
    void delete() {
        var message1 = Instancio.create(PositionMessage.class);
        var message2 = Instancio.create(PositionMessage.class);
        var entity = Instancio.create(Position.class);
        doReturn(Optional.of(entity)).when(service).get(message1.getId());
        doReturn(Optional.empty()).when(service).get(message2.getId());
        doNothing().when(service).delete(entity);
        provider.delete(message1);
        provider.delete(message2);
        verify(service, times(2)).get(any(UUID.class));
        verify(service).delete(any(Position.class));
    }
    
    @Test
    void save() {
        var message1 = Instancio.create(PositionMessage.class);
        var message2 = Instancio.create(PositionMessage.class);
        var message3 = Instancio.of(PositionMessage.class)
                                .set(field(PositionMessage::getOrganizationId), null)
                                .create();
        var entity1 = Instancio.create(Position.class);
        var entity2 = Instancio.create(Position.class);
        var organization = Instancio.create(Organization.class);
        doReturn(entity1).when(mapper).positionMessageToPosition(message1);
        doReturn(entity2).when(mapper).positionMessageToPosition(message2);
        doReturn(Optional.of(organization)).when(organizationService).get(message1.getOrganizationId());
        doReturn(Optional.empty()).when(organizationService).get(message2.getOrganizationId());
        doReturn(entity1).when(service).save(positionArgumentCaptor.capture());
        doNothing().when(organizationService).saveGrpcEntity(
                "Can't save position id:%s, entityName:%s, organizationId:%s, awaiting organization synchronization".formatted(message2.getId(),
                                                                                                                               message2.getPositionName(),
                                                                                                                               message2.getOrganizationId()),
                message2.getOrganizationId());
        provider.save(message1);
        provider.save(message2);
        provider.save(message3);
        assertThat(positionArgumentCaptor.getAllValues()).hasSize(2);
        assertThat(positionArgumentCaptor.getAllValues().get(0))
                .usingRecursiveComparison()
                .isEqualTo(entity1);
        assertThat(positionArgumentCaptor.getAllValues().get(1))
                .usingRecursiveComparison()
                .isEqualTo(entity2);
        verify(mapper, times(2)).positionMessageToPosition(any(PositionMessage.class));
        verify(service, times(2)).save(any(Position.class));
        verify(organizationService, times(2)).get(any(UUID.class));
        verify(organizationService).saveGrpcEntity(anyString(), any(UUID.class));
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo(PositionProviderImpl.class.getName());
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo("Can't save position id:%s, name:%s, organizationId:%s, organization isn't present".formatted(message3.getId(),
                                                                                                                         message3.getPositionName(),
                                                                                                                         message3.getOrganizationId()));
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    }
}