package ru.sberbank.ditsib.transport.vehicle.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.database.model.Position;
import ru.sberbank.ditsib.transport.vehicle.mapper.EmployeeMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.impl.EmployeeProviderImpl;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.PositionService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера сотрудников")
class EmployeeProviderTest {

    @InjectMocks
    private EmployeeProviderImpl provider;

    @Mock
    private EmployeeService service;

    @Mock
    private EmployeeMapper mapper;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private PositionService positionService;

    private Department department;
    private Position position;
    private Employee employee;
    private EmployeeMessage message;
    private Organization organization;

    @BeforeEach
    void setup() {
        organization = Organization.builder()
                .id(UUID.randomUUID())
                .digitId(1L)
                .officialName("officialName")
                .build();
        department = Department.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .departmentName("departmentName")
                .humanReadableId("humanReadableId")
                .build();
        position = Position.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .positionName("positionName")
                .build();
        employee = Employee.builder()
                .id(UUID.randomUUID())
                .department(department)
                .position(position)
                .lastName("lastName")
                .firstName("firstName")
                .mobilePhone("mobilePhone")
                .patronymic("patronymic")
                .department(department)
                .humanReadableId("humanReadableId")
                .personnelNumber("personnelNumber")
                .userId(UUID.randomUUID())
                .organization(organization)
                .build();
        message = EmployeeMessage.builder()
                .id(employee.getId())
                .personnelNumber(employee.getPersonnelNumber())
                .mobilePhone(employee.getMobilePhone())
                .patronymic(employee.getPatronymic())
                .lastName(employee.getLastName())
                .firstName(employee.getFirstName())
                .positionId(position.getId())
                .userId(employee.getUserId())
                .departmentId(department.getId())
                .organizationId(organization.getId())
                .humanReadableId(employee.getHumanReadableId())
                .organizationId(organization.getId())
                .build();
    }

    @Test
    void delete() {
        when(mapper.employeeMessageToEmployee(message)).thenReturn(employee);
        doNothing().when(service).delete(employee);
        provider.delete(message);
        verify(mapper).employeeMessageToEmployee(message);
        verify(service).delete(employee);
    }

    @Test
    void saveOrUpdate() {
        when(mapper.employeeMessageToEmployee(message)).thenReturn(employee);
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionService.get(message.getPositionId())).thenReturn(Optional.of(position));
        provider.save(message);
        verify(mapper, times(1)).employeeMessageToEmployee(message);
        verify(service, times(1)).saveOrUpdate(employee);
        verify(departmentService, times(1)).get(message.getDepartmentId());
        verify(positionService, times(1)).get(message.getPositionId());
    }

    @Test
    void saveOrUpdateNoDepartment() {
        var messageNoDepartment = EmployeeMessage.builder()
                .id(employee.getId())
                .personnelNumber(employee.getPersonnelNumber())
                .mobilePhone(employee.getMobilePhone())
                .patronymic(employee.getPatronymic())
                .lastName(employee.getLastName())
                .firstName(employee.getFirstName())
                .positionId(position.getId())
                .userId(employee.getUserId())
                .departmentId(null)
                .organizationId(organization.getId())
                .humanReadableId(employee.getHumanReadableId())
                .build();
        when(mapper.employeeMessageToEmployee(messageNoDepartment)).thenReturn(employee);
        employee.setDepartment(null);
        provider.save(messageNoDepartment);
        verify(mapper, times(1)).employeeMessageToEmployee(messageNoDepartment);
        verify(service, times(0)).saveOrUpdate(employee);
        verify(departmentService, times(0)).get(messageNoDepartment.getDepartmentId());
        verify(positionService, times(0)).get(messageNoDepartment.getPositionId());
    }

    @Test
    void saveOrUpdateNoDepartmentInDatabase() {
        when(mapper.employeeMessageToEmployee(message)).thenReturn(employee);
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(EntityNotFoundException.class);
        verify(mapper, times(1)).employeeMessageToEmployee(message);
        verify(service, times(0)).saveOrUpdate(employee);
        verify(departmentService, times(1)).get(message.getDepartmentId());
        verify(positionService, times(0)).get(message.getPositionId());
    }

    @Test
    void saveOrUpdateNoPosition() {
        var messageNoPosition = EmployeeMessage.builder()
                .id(employee.getId())
                .personnelNumber(employee.getPersonnelNumber())
                .mobilePhone(employee.getMobilePhone())
                .patronymic(employee.getPatronymic())
                .lastName(employee.getLastName())
                .firstName(employee.getFirstName())
                .positionId(null)
                .userId(employee.getUserId())
                .departmentId(department.getId())
                .organizationId(organization.getId())
                .humanReadableId(employee.getHumanReadableId())
                .build();
        when(mapper.employeeMessageToEmployee(messageNoPosition)).thenReturn(employee);
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.of(department));
        employee.setPosition(null);
        provider.save(messageNoPosition);
        verify(mapper, times(1)).employeeMessageToEmployee(messageNoPosition);
        verify(service, times(0)).saveOrUpdate(employee);
        verify(departmentService, times(1)).get(messageNoPosition.getDepartmentId());
        verify(positionService, times(0)).get(messageNoPosition.getPositionId());
    }

    @Test
    void saveOrUpdateNoPositionInDatabase() {
        when(mapper.employeeMessageToEmployee(message)).thenReturn(employee);
        when(departmentService.get(message.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionService.get(message.getPositionId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> provider.save(message))
                .isInstanceOf(EntityNotFoundException.class);
        verify(mapper, times(1)).employeeMessageToEmployee(message);
        verify(service, times(0)).saveOrUpdate(employee);
        verify(departmentService, times(1)).get(message.getDepartmentId());
        verify(positionService, times(1)).get(message.getPositionId());
    }
}