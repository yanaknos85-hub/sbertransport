package ru.sber.transport.telemechanic.provider.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.mapper.DepartmentMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера подразделений")
class DepartmentProviderImplTest {
    
    @InjectMocks
    private DepartmentProviderImpl provider;
    @Mock
    private DepartmentService service;
    @Mock
    private DepartmentMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @Captor
    private ArgumentCaptor<Department> departmentArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(DepartmentProviderImpl.class);
    
    @Test
    void delete() {
        var message1 = Instancio.create(DepartmentMessage.class);
        var message2 = Instancio.create(DepartmentMessage.class);
        var entity = Instancio.create(Department.class);
        doReturn(Optional.of(entity)).when(service).get(message1.getId());
        doReturn(Optional.empty()).when(service).get(message2.getId());
        doNothing().when(service).delete(entity);
        provider.delete(message1);
        provider.delete(message2);
        verify(service, times(2)).get(any(UUID.class));
        verify(service).delete(any(Department.class));
    }
    
    @Test
    @DisplayName("Сохранение, есть идентификатор организации, организация есть в БД, нет родительского подразделения")
    void saveHaveOrganizationNoParent() {
        var message = Instancio.of(DepartmentMessage.class)
                               .set(field(DepartmentMessage::getParentId), null)
                               .create();
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), null)
                              .create();
        var organization = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).departmentMessageToDepartment(message);
        doReturn(Optional.of(organization)).when(organizationService).get(message.getOrganizationId());
        doReturn(entity).when(service).save(departmentArgumentCaptor.capture());
        provider.save(message);
        assertThat(departmentArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(departmentArgumentCaptor.getAllValues().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService, never()).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service, never()).get(any(UUID.class));
        verify(service, never()).saveGrpcParentEntities(any(UUID.class));
        verify(mapper).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @Test
    @DisplayName("Сохранение, нет идентификатора организации")
    void saveNoOrganization() {
        var message = Instancio.of(DepartmentMessage.class)
                               .set(field(DepartmentMessage::getOrganizationId), null)
                               .create();
        provider.save(message);
        verify(organizationService, never()).get(any(UUID.class));
        verify(organizationService, never()).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service, never()).get(any(UUID.class));
        verify(service, never()).saveGrpcParentEntities(any(UUID.class));
        verify(mapper, never()).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service, never()).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents())
                .hasSize(1)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getFormattedMessage
                           )
                .containsExactly(
                        tuple(
                                Level.INFO,
                                "Can't save department id:%s, name:%s, organizationId:%s, organization isn't present".formatted(message.getId(),
                                                                                                                                message.getDepartmentName(),
                                                                                                                                message.getOrganizationId())
                             )
                                );
    }
    
    @Test
    @DisplayName("Сохранение, есть идентификатор организации, организации нет в БД, организацию вернули по grpc")
    void saveHaveOrganizationByGrpc() {
        var message = Instancio.of(DepartmentMessage.class)
                               .set(field(DepartmentMessage::getParentId), null)
                               .create();
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), null)
                              .create();
        doReturn(entity).when(mapper).departmentMessageToDepartment(message);
        doReturn(Optional.empty()).when(organizationService).get(message.getOrganizationId());
        doNothing().when(organizationService).saveGrpcEntity(
                "Can't save department id:%s, entityName:%s, organizationId:%s, awaiting organization synchronization".formatted(message.getId(),
                                                                                                                                 message.getDepartmentName(),
                                                                                                                                 message.getOrganizationId()),
                message.getOrganizationId());
        doReturn(entity).when(service).save(departmentArgumentCaptor.capture());
        provider.save(message);
        assertThat(departmentArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(departmentArgumentCaptor.getAllValues().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service, never()).get(any(UUID.class));
        verify(service, never()).saveGrpcParentEntities(any(UUID.class));
        verify(mapper).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @Test
    @DisplayName("Сохранение, есть идентификатор организации, организации нет в БД, организацию не вернули по grpc")
    void saveNoOrganizationByGrpc() {
        var message = Instancio.of(DepartmentMessage.class)
                               .set(field(DepartmentMessage::getParentId), null)
                               .create();
        var exceptionMessage = "Awaiting an organization synchronization";
        doReturn(Optional.empty()).when(organizationService).get(message.getOrganizationId());
        doThrow(new AwaitingSynchronizationException(exceptionMessage)).when(organizationService).saveGrpcEntity(
                "Can't save department id:%s, entityName:%s, organizationId:%s, awaiting organization synchronization".formatted(message.getId(),
                                                                                                                                 message.getDepartmentName(),
                                                                                                                                 message.getOrganizationId()),
                message.getOrganizationId());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(AwaitingSynchronizationException.class)
                .hasMessage(exceptionMessage);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service, never()).get(any(UUID.class));
        verify(service, never()).saveGrpcParentEntities(any(UUID.class));
        verify(mapper, never()).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service, never()).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @Test
    @DisplayName("Сохранение, есть родительское подразделение в БД")
    void saveHaveParentInDb() {
        var message = Instancio.create(DepartmentMessage.class);
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), message.getParentId())
                              .create();
        var parentEntity = Instancio.create(Department.class);
        var organization = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).departmentMessageToDepartment(message);
        doReturn(Optional.of(organization)).when(organizationService).get(message.getOrganizationId());
        doReturn(entity).when(service).save(departmentArgumentCaptor.capture());
        doReturn(Optional.of(parentEntity)).when(service).get(message.getParentId());
        provider.save(message);
        assertThat(departmentArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(departmentArgumentCaptor.getAllValues().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService, never()).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service).get(any(UUID.class));
        verify(service, never()).saveGrpcParentEntities(any(UUID.class));
        verify(mapper).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @Test
    @DisplayName("Сохранение, нет родительского подразделения в БД, есть родительское подразделение по grpc")
    void saveHaveParentByGrpc() {
        var message = Instancio.create(DepartmentMessage.class);
        var entity = Instancio.of(Department.class)
                              .set(field(Department::getParentId), message.getParentId())
                              .create();
        var organization = Instancio.create(Organization.class);
        doReturn(entity).when(mapper).departmentMessageToDepartment(message);
        doReturn(Optional.of(organization)).when(organizationService).get(message.getOrganizationId());
        doReturn(entity).when(service).save(departmentArgumentCaptor.capture());
        doReturn(Optional.empty()).when(service).get(message.getParentId());
        doNothing().when(service).saveGrpcParentEntities(message.getParentId());
        provider.save(message);
        assertThat(departmentArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(departmentArgumentCaptor.getAllValues().getFirst())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService, never()).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service).get(any(UUID.class));
        verify(service).saveGrpcParentEntities(any(UUID.class));
        verify(mapper).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @Test
    @DisplayName("Сохранение, нет родительского подразделения по grpc")
    void saveNoParentByGrpc() {
        var message = Instancio.create(DepartmentMessage.class);
        var organization = Instancio.create(Organization.class);
        var exceptionMessage = "Awaiting a parent department synchronization, parentId:" + message.getParentId();
        doReturn(Optional.of(organization)).when(organizationService).get(message.getOrganizationId());
        doReturn(Optional.empty()).when(service).get(message.getParentId());
        doThrow(new AwaitingSynchronizationException(exceptionMessage)).when(service).saveGrpcParentEntities(message.getParentId());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(AwaitingSynchronizationException.class)
                .hasMessage(exceptionMessage);
        verify(organizationService).get(any(UUID.class));
        verify(organizationService, never()).saveGrpcEntity(anyString(), any(UUID.class));
        verify(service).get(any(UUID.class));
        verify(service).saveGrpcParentEntities(any(UUID.class));
        verify(mapper, never()).departmentMessageToDepartment(any(DepartmentMessage.class));
        verify(service, never()).save(any(Department.class));
        assertThat(LOGGING_EXTENSION.getEvents()).isEmpty();
    }
    
    @ParameterizedTest
    @CsvSource({
            "true, true",
            "true, false",
            "false, false"
    })
    void addAutopark(boolean departmentExists, boolean routingIdMatches) {
        var departmentId = UUID.randomUUID();
        var autoparkId = UUID.randomUUID();
        var messageRoutingId = routingIdMatches ? departmentId : UUID.randomUUID();
        
        var autoparkMessage = Instancio.of(AutoparkMessage.class)
                                       .set(field(AutoparkMessage::id), autoparkId)
                                       .set(field(AutoparkMessage::routingId), messageRoutingId)
                                       .create();
        
        var department = new Department();
        department.setId(departmentId);
        department.setAutoparkId(null);
        
        if (departmentExists && routingIdMatches) {
            when(service.get(any(UUID.class))).thenReturn(Optional.of(department));
            when(service.save(any(Department.class))).thenReturn(department);
        } else {
            when(service.get(any(UUID.class))).thenReturn(Optional.empty());
        }
        
        provider.addAutopark(autoparkMessage);
        verify(service).get(any(UUID.class));
        
        if (departmentExists && routingIdMatches) {
            verify(service).save(any(Department.class));
            Assertions.assertEquals(department.getAutoparkId(), autoparkId);
        } else {
            verify(service, never()).save(any(Department.class));
        }
    }
    
    @ParameterizedTest
    @CsvSource({
            "true, true",
            "true, false",
            "false, false"
    })
    void deleteAutopark(boolean departmentExists, boolean autoparkIdMatches) {
        var departmentId = UUID.randomUUID();
        var autoparkId = UUID.randomUUID();
        var messageAutoparkId = autoparkIdMatches ? autoparkId : UUID.randomUUID();
        
        var autoparkMessage = Instancio.of(AutoparkMessage.class)
                                       .set(field(AutoparkMessage::id), messageAutoparkId)
                                       .create();
        
        var department = new Department();
        department.setId(departmentId);
        department.setAutoparkId(autoparkId);
        
        if (departmentExists && autoparkIdMatches) {
            when(service.getByAutoparkId(any(UUID.class))).thenReturn(Optional.of(department));
            when(service.save(any(Department.class))).thenReturn(department);
        } else {
            when(service.getByAutoparkId(any(UUID.class))).thenReturn(Optional.empty());
        }
        
        provider.deleteAutopark(autoparkMessage);
        verify(service).getByAutoparkId(any(UUID.class));
        
        if (departmentExists && autoparkIdMatches) {
            verify(service).save(any(Department.class));
            Assertions.assertNull(department.getAutoparkId());
        } else {
            verify(service, never()).save(any(Department.class));
        }
    }
}