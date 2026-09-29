package ru.sber.transport.telemechanic.service.grpc.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.telemechanic.LoggingExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PositionsImplTest {
    
    @InjectMocks
    private PositionsImpl positions;
    @Mock
    private PositionsGrpc.PositionsBlockingStub positionsBlockingStub;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(PositionsImpl.class);
    
    @Test
    void one() {
        var id = UUID.randomUUID();
        var response = OrganizationsOuterClass.Position.newBuilder()
                                                       .setId(id.toString())
                                                       .setHumanReadableId(UUID.randomUUID().toString())
                                                       .setOrganizationId(UUID.randomUUID().toString())
                                                       .setName(UUID.randomUUID().toString())
                                                       .setDeleted(false)
                                                       .setNoApproveRequired(false)
                                                       .build();
        doReturn(response).when(positionsBlockingStub).one(any());
        var actual = positions.one(id);
        assertThat(actual.getId()).isEqualTo(UUID.fromString(response.getId()));
        assertThat(actual.getOrganization().getId()).isEqualTo(UUID.fromString(response.getOrganizationId()));
        assertThat(actual.getPositionName()).isEqualTo(response.getName());
        assertThat(actual.isActive()).isEqualTo(!response.getDeleted());
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().getFirst();
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(PositionsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Position %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(PositionsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isEqualTo("Position %s responded", id);
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.INFO);
    }
    
    @Test
    void oneWithException() {
        var id = UUID.randomUUID();
        doThrow(JUnitException.class).when(positionsBlockingStub).one(any());
        positions.one(id);
        assertEquals(3, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().getFirst();
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(PositionsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Position %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(PositionsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isNull();
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.DEBUG);
        var loggingEvent3 = LOGGING_EXTENSION.getEvents().get(2);
        assertThat(loggingEvent3.getLoggerName()).isEqualTo(PositionsImpl.class.getName());
        assertThat(loggingEvent3.getFormattedMessage())
                .isEqualTo("Failed to receive position, id: %s, cause: null", id);
        assertThat(loggingEvent3.getLevel()).isEqualTo(Level.INFO);
    }
}