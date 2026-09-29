package ru.sber.transport.telemechanic.provider.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
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
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.mapper.EmployeeMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера сотрудников")
class EmployeeProviderImplTest {
    
    @InjectMocks
    private EmployeeProviderImpl provider;
    @Mock
    private EmployeeService service;
    @Mock
    private EmployeeMapper mapper;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PositionService positionService;
    @Captor
    private ArgumentCaptor<Employee> employeeArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(EmployeeProviderImpl.class);
    
    @Test
    void delete() {
        var message1 = Instancio.create(EmployeeMessage.class);
        var message2 = Instancio.create(EmployeeMessage.class);
        var entity = Instancio.create(Employee.class);
        doReturn(Optional.of(entity)).when(service).get(message1.getId());
        doReturn(Optional.empty()).when(service).get(message2.getId());
        doNothing().when(service).delete(entity);
        provider.delete(message1);
        provider.delete(message2);
        verify(service, times(2)).get(any(UUID.class));
        verify(service).delete(any(Employee.class));
    }
    
    @Test
    void save() {
        var message1 = Instancio.create(EmployeeMessage.class);
        var message2 = Instancio.create(EmployeeMessage.class);
        var message3 = Instancio.create(EmployeeMessage.class);
        var message4 = Instancio.create(EmployeeMessage.class);
        var message5 = Instancio.create(EmployeeMessage.class);
        var message6 = Instancio.of(EmployeeMessage.class)
                                .set(field(EmployeeMessage::getOrganizationId), null)
                                .create();
        var message7 = Instancio.of(EmployeeMessage.class)
                                .set(field(EmployeeMessage::getDepartmentId), null)
                                .create();
        var message8 = Instancio.of(EmployeeMessage.class)
                                .set(field(EmployeeMessage::getPositionId), null)
                                .create();
        var entity1 = Instancio.create(Employee.class);
        var entity2 = Instancio.create(Employee.class);
        var entity3 = Instancio.create(Employee.class);
        var entity4 = Instancio.create(Employee.class);
        var entity5 = Instancio.create(Employee.class);
        var organization = Instancio.create(Organization.class);
        var department = Instancio.create(Department.class);
        var position = Instancio.create(Position.class);
        doReturn(entity1).when(mapper).employeeMessageToEmployee(message1);
        doReturn(entity2).when(mapper).employeeMessageToEmployee(message2);
        doReturn(entity3).when(mapper).employeeMessageToEmployee(message3);
        doReturn(entity4).when(mapper).employeeMessageToEmployee(message4);
        doReturn(Optional.of(organization)).when(organizationService).get(message1.getOrganizationId());
        doReturn(Optional.empty()).when(organizationService).get(message2.getOrganizationId());
        doReturn(Optional.of(organization)).when(organizationService).get(message3.getOrganizationId());
        doReturn(Optional.of(organization)).when(organizationService).get(message4.getOrganizationId());
        doReturn(Optional.of(organization)).when(organizationService).get(message5.getOrganizationId());
        doReturn(Optional.of(department)).when(departmentService).get(message1.getDepartmentId());
        doReturn(Optional.of(department)).when(departmentService).get(message2.getDepartmentId());
        doReturn(Optional.empty()).when(departmentService).get(message3.getDepartmentId());
        doReturn(Optional.of(department)).when(departmentService).get(message4.getDepartmentId());
        doReturn(Optional.of(department)).when(departmentService).get(message5.getDepartmentId());
        doReturn(Optional.of(position)).when(positionService).get(message1.getPositionId());
        doReturn(Optional.of(position)).when(positionService).get(message2.getPositionId());
        doReturn(Optional.of(position)).when(positionService).get(message3.getPositionId());
        doReturn(Optional.empty()).when(positionService).get(message4.getPositionId());
        doReturn(Optional.of(position)).when(positionService).get(message5.getPositionId());
        doReturn(Optional.empty()).when(service).get(message1.getId());
        doReturn(Optional.empty()).when(service).get(message2.getId());
        doReturn(Optional.empty()).when(service).get(message3.getId());
        doReturn(Optional.empty()).when(service).get(message4.getId());
        doReturn(Optional.of(entity5)).when(service).get(message5.getId());
        doReturn(entity1).when(service).save(employeeArgumentCaptor.capture());
        doNothing().when(organizationService).saveGrpcEntity(
                "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, awaiting organization synchronization"
                        .formatted(message2.getId(),
                                   message2.getPersonnelNumber(),
                                   message2.getPositionId(),
                                   message2.getOrganizationId(),
                                   message2.getDepartmentId()),
                message2.getOrganizationId());
        doNothing().when(departmentService).saveGrpcEntity(
                "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, awaiting department synchronization"
                        .formatted(message3.getId(),
                                   message3.getPersonnelNumber(),
                                   message3.getPositionId(),
                                   message3.getOrganizationId(),
                                   message3.getDepartmentId()),
                message3.getDepartmentId());
        doNothing().when(positionService).saveGrpcEntity(
                "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, awaiting position synchronization"
                        .formatted(message4.getId(),
                                   message4.getPersonnelNumber(),
                                   message4.getPositionId(),
                                   message4.getOrganizationId(),
                                   message4.getDepartmentId()),
                message4.getPositionId());
        provider.save(message1);
        provider.save(message2);
        provider.save(message3);
        provider.save(message4);
        provider.save(message5);
        provider.save(message6);
        provider.save(message7);
        provider.save(message8);
        assertThat(employeeArgumentCaptor.getAllValues()).hasSize(5);
        assertThat(employeeArgumentCaptor.getAllValues().get(0))
                .usingRecursiveComparison()
                .isEqualTo(entity1);
        assertThat(employeeArgumentCaptor.getAllValues().get(1))
                .usingRecursiveComparison()
                .isEqualTo(entity2);
        assertThat(employeeArgumentCaptor.getAllValues().get(2))
                .usingRecursiveComparison()
                .isEqualTo(entity3);
        assertThat(employeeArgumentCaptor.getAllValues().get(3))
                .usingRecursiveComparison()
                .isEqualTo(entity4);
        checkUpdatedEntity(message5, organization, department, position);
        verify(mapper, times(4)).employeeMessageToEmployee(any(EmployeeMessage.class));
        verify(service, times(5)).save(any(Employee.class));
        verify(service, times(5)).get(any(UUID.class));
        verify(organizationService, times(5)).get(any(UUID.class));
        verify(departmentService, times(5)).get(any(UUID.class));
        verify(positionService, times(5)).get(any(UUID.class));
        verify(organizationService).saveGrpcEntity(anyString(), any(UUID.class));
        verify(departmentService).saveGrpcEntity(anyString(), any(UUID.class));
        verify(positionService).saveGrpcEntity(anyString(), any(UUID.class));
        assertEquals(3, LOGGING_EXTENSION.getEvents().size());
        checkLog(LOGGING_EXTENSION.getEvents().get(0),
                 "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, organization isn't present"
                         .formatted(message6.getId(),
                                    message6.getPersonnelNumber(),
                                    message6.getPositionId(),
                                    message6.getOrganizationId(),
                                    message6.getDepartmentId())
                );
        checkLog(LOGGING_EXTENSION.getEvents().get(1),
                 "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, department isn't present"
                         .formatted(message7.getId(),
                                    message7.getPersonnelNumber(),
                                    message7.getPositionId(),
                                    message7.getOrganizationId(),
                                    message7.getDepartmentId())
                );
        checkLog(LOGGING_EXTENSION.getEvents().get(2),
                 "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, position isn't present"
                         .formatted(message8.getId(),
                                    message8.getPersonnelNumber(),
                                    message8.getPositionId(),
                                    message8.getOrganizationId(),
                                    message8.getDepartmentId())
                );
    }
    
    private void checkUpdatedEntity(EmployeeMessage message5, Organization organization, Department department, Position position) {
        var updatedActual = employeeArgumentCaptor.getAllValues().get(4);
        assertThat(updatedActual.isActive()).isTrue();
        assertThat(updatedActual.getOrganization())
                .usingRecursiveComparison()
                .isEqualTo(organization);
        assertThat(updatedActual.getDepartment())
                .usingRecursiveComparison()
                .isEqualTo(department);
        assertThat(updatedActual.getPosition())
                .usingRecursiveComparison()
                .isEqualTo(position);
        assertThat(updatedActual.getFirstName()).isEqualTo(message5.getFirstName());
        assertThat(updatedActual.getLastName()).isEqualTo(message5.getLastName());
        assertThat(updatedActual.getPatronymic()).isEqualTo(message5.getPatronymic());
        assertThat(updatedActual.getPersonnelNumber()).isEqualTo(message5.getPersonnelNumber());
        assertThat(updatedActual.getHumanReadableId()).isEqualTo(message5.getHumanReadableId());
        assertThat(updatedActual.getMobilePhone()).isEqualTo(message5.getMobilePhone());
        assertThat(updatedActual.getUserId()).isEqualTo(message5.getUserId());
    }
    
    private void checkLog(ILoggingEvent loggingEvent, String message) {
        assertThat(loggingEvent.getLoggerName()).isEqualTo(EmployeeProviderImpl.class.getName());
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo(message);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    }
}