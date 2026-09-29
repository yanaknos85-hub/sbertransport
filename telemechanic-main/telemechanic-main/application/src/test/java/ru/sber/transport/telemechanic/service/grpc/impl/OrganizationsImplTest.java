package ru.sber.transport.telemechanic.service.grpc.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.Organization;

import java.util.Collections;
import java.util.UUID;

import static com.google.protobuf.NullValue.NULL_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrganizationsImplTest {
    
    @InjectMocks
    private OrganizationsImpl organizations;
    @Mock
    private OrganizationsGrpc.OrganizationsBlockingStub organizationsBlockingStub;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(OrganizationsImpl.class);
    
    @Test
    void one() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var response1 = OrganizationsOuterClass.Organization.newBuilder()
                                                            .setId(id1.toString())
                                                            .setDigitId(1)
                                                            .setName(UUID.randomUUID().toString())
                                                            .setAddress(UUID.randomUUID().toString())
                                                            .setMsrn(UUID.randomUUID().toString())
                                                            .setTid(UUID.randomUUID().toString())
                                                            .setCode(OrganizationsOuterClass.NullableInt.newBuilder()
                                                                                                        .setValue(1)
                                                                                                        .build())
                                                            .setDeleted(false)
                                                            .setGroup(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                            .setValue(UUID.randomUUID().toString())
                                                                                                            .build())
                                                            .setType(OrganizationsOuterClass.StructureType.INTERNAL)
                                                            .build();
        var response2 = OrganizationsOuterClass.Organization.newBuilder()
                                                            .setId(id2.toString())
                                                            .setDigitId(1)
                                                            .setName(UUID.randomUUID().toString())
                                                            .setAddress(UUID.randomUUID().toString())
                                                            .setMsrn(UUID.randomUUID().toString())
                                                            .setTid(UUID.randomUUID().toString())
                                                            .setCode(OrganizationsOuterClass.NullableInt.newBuilder()
                                                                                                        .setNull(NULL_VALUE)
                                                                                                        .build())
                                                            .setDeleted(false)
                                                            .setGroup(OrganizationsOuterClass.NullableString.newBuilder()
                                                                                                            .setNull(NULL_VALUE)
                                                                                                            .build())
                                                            .setType(OrganizationsOuterClass.StructureType.INTERNAL)
                                                            .build();
        doReturn(response1, response2).when(organizationsBlockingStub).one(any());
        var actual1 = organizations.one(id1);
        var actual2 = organizations.one(id2);
        assertThat(actual1).extracting(
                                   Organization::getId,
                                   Organization::getDigitId,
                                   Organization::getOfficialName,
                                   Organization::getMsrn,
                                   Organization::getTin,
                                   Organization::getOrganizationGroupId,
                                   Organization::isActive,
                                   Organization::getContacts,
                                   Organization::getAddress
                                      )
                           .containsExactly(
                                   UUID.fromString(response1.getId()),
                                   (long) response1.getDigitId(),
                                   response1.getName(),
                                   response1.getMsrn(),
                                   response1.getTid(),
                                   response1.getGroup().hasNull() ? null : UUID.fromString(response1.getGroup().getValue()),
                                   !response1.getDeleted(),
                                   Collections.emptySet(),
                                   null);
        assertThat(actual2).extracting(
                                   Organization::getId,
                                   Organization::getDigitId,
                                   Organization::getOfficialName,
                                   Organization::getMsrn,
                                   Organization::getTin,
                                   Organization::getOrganizationGroupId,
                                   Organization::isActive,
                                   Organization::getContacts,
                                   Organization::getAddress
                                      )
                           .containsExactly(
                                   UUID.fromString(response2.getId()),
                                   (long) response2.getDigitId(),
                                   response2.getName(),
                                   response2.getMsrn(),
                                   response2.getTid(),
                                   response2.getGroup().hasNull() ? null : UUID.fromString(response2.getGroup().getValue()),
                                   !response2.getDeleted(),
                                   Collections.emptySet(),
                                   null);
        assertEquals(4, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().getFirst();
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Organization %s not found. Requesting from source", id1);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isEqualTo("Organization %s responded", id1);
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent3 = LOGGING_EXTENSION.getEvents().get(2);
        assertThat(loggingEvent3.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent3.getFormattedMessage())
                .isEqualTo("Organization %s not found. Requesting from source", id2);
        assertThat(loggingEvent3.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent4 = LOGGING_EXTENSION.getEvents().get(3);
        assertThat(loggingEvent4.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent4.getFormattedMessage())
                .isEqualTo("Organization %s responded", id2);
        assertThat(loggingEvent4.getLevel()).isEqualTo(Level.INFO);
    }
    
    @Test
    void oneWithException() {
        var id = UUID.randomUUID();
        doThrow(JUnitException.class).when(organizationsBlockingStub).one(any());
        organizations.one(id);
        assertEquals(3, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent1 = LOGGING_EXTENSION.getEvents().getFirst();
        assertThat(loggingEvent1.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent1.getFormattedMessage())
                .isEqualTo("Organization %s not found. Requesting from source", id);
        assertThat(loggingEvent1.getLevel()).isEqualTo(Level.INFO);
        var loggingEvent2 = LOGGING_EXTENSION.getEvents().get(1);
        assertThat(loggingEvent2.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent2.getFormattedMessage())
                .isNull();
        assertThat(loggingEvent2.getLevel()).isEqualTo(Level.DEBUG);
        var loggingEvent3 = LOGGING_EXTENSION.getEvents().get(2);
        assertThat(loggingEvent3.getLoggerName()).isEqualTo(OrganizationsImpl.class.getName());
        assertThat(loggingEvent3.getFormattedMessage())
                .isEqualTo("Failed to receive organization, id: %s, cause: null", id);
        assertThat(loggingEvent3.getLevel()).isEqualTo(Level.INFO);
    }
}