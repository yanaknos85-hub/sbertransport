package ru.sber.transport.telemechanic.service.impl;

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
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.dto.DepartmentDto;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.mapper.OrganizationMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.grpc.Organizations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с организациями")
class OrganizationServiceImplTest {
    
    @InjectMocks
    private OrganizationServiceImpl service;
    @Mock
    private OrganizationRepository repository;
    @Mock
    private OrganizationMapper organizationMapper;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private Organizations organizations;
    @Captor
    private ArgumentCaptor<Organization> captor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(OrganizationServiceImpl.class);
    
    @Test
    void getTest() {
        var entity = Instancio.create(Organization.class);
        var notExistId = UUID.randomUUID();
        doReturn(Optional.of(entity)).when(repository).findById(entity.getId());
        doReturn(Optional.empty()).when(repository).findById(notExistId);
        assertThat(service.get(entity.getId())).isEqualTo(Optional.of(entity));
        assertThat(service.get(notExistId)).isNotPresent();
    }
    
    @Test
    void delete() {
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(repository).save(captor.capture());
        service.delete(entity);
        var savedEntity = captor.getValue();
        assertThat(savedEntity)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(entity);
        assertThat(savedEntity.isActive()).isFalse();
        verify(repository).save(any(Organization.class));
    }
    
    @Test
    void save() {
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(repository).save(captor.capture());
        var actual = service.save(entity);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(entity);
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(entity);
        verify(repository).save(any(Organization.class));
    }
    
    @Test
    void getAll() {
        var expected = Instancio.createList(OrganizationDto.class);
        doReturn(expected).when(repository).findAllActiveOrderByOfficialName();
        assertThat(service.getAll())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    void getAllWithInternalContractors() {
        var expected = Instancio.createList(OrganizationDto.class);
        doReturn(expected).when(repository).findAllActiveWithInternalContractorOrderByOfficialName();
        assertThat(service.getAllWithInternalContractor())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    void getAllWithDepartment() {
        var organization1 = Instancio.create(Organization.class);
        var organization2 = Organization.builder()
                                        .id(UUID.randomUUID())
                                        .officialName("officialName2")
                                        .digitId(2L)
                                        .msrn("22222222")
                                        .tin("222222")
                                        .build();
        var uuids = Set.of(organization1.getId(), organization2.getId());
        var departmentDto1 = new DepartmentDto(UUID.randomUUID(), "departmentName1", null);
        var departmentDto2 = new DepartmentDto(UUID.randomUUID(), "departmentName2", departmentDto1.id());
        var departmentDto3 = new DepartmentDto(UUID.randomUUID(), "departmentName3", departmentDto1.id());
        var departmentDtoList = List.of(departmentDto1, departmentDto2, departmentDto3);
        when(repository.findAllByIdInOrderByOfficialName(uuids)).thenReturn(List.of(organization1, organization2));
        when(departmentService.getByOrganizationId(organization1.getId())).thenReturn(departmentDtoList);
        when(departmentService.getByOrganizationId(organization2.getId())).thenReturn(Collections.emptyList());
        var result = service.getAllWithDepartment(uuids);
        assertEquals(2, result.size());
        var actual1 = result.get(0);
        var actual2 = result.get(1);
        assertEquals(organization1.getOfficialName(), actual1.officialName());
        assertEquals(departmentDtoList, actual1.departmentDtoList());
        assertEquals(organization2.getOfficialName(), actual2.officialName());
        assertEquals(Collections.emptyList(), actual2.departmentDtoList());
    }
    
    @Test
    void getByUserId() {
        var organization = Instancio.create(Organization.class);
        var department = Instancio.create(Department.class);
        var position = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        var employee = Instancio.of(Employee.class)
                                .set(field(Employee::getOrganization), organization)
                                .set(field(Employee::getDepartment), department)
                                .set(field(Employee::getPosition), position)
                                .create();
        var expected1 = new OrganizationDto(organization.getId(), organization.getOfficialName());
        when(employeeService.getByUserId(employee.getUserId())).thenReturn(employee);
        when(organizationMapper.organizationToOrganizationDto(organization)).thenReturn(expected1);
        assertEquals(expected1, service.getByUserId(employee.getUserId()));
    }
    
    @Test
    void getFirstContactPhone() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var phone = UUID.randomUUID().toString();
        doReturn(phone).when(repository).getFirstContactPhone(id1);
        doReturn(null).when(repository).getFirstContactPhone(id2);
        assertThat(service.getFirstContactPhone(id1)).isEqualTo(phone);
        assertThat(service.getFirstContactPhone(id2)).isNull();
    }
    
    @Test
    void saveGrpcEntity() {
        var message = UUID.randomUUID().toString();
        var id = UUID.randomUUID();
        var wrongId = UUID.randomUUID();
        var entity = Instancio.create(Organization.class);
        doReturn(entity).when(organizations).one(id);
        doReturn(null).when(organizations).one(wrongId);
        doReturn(entity).when(repository).save(captor.capture());
        service.saveGrpcEntity(message, id);
        var actual = captor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(entity);
        assertThatExceptionOfType(AwaitingSynchronizationException.class)
                .isThrownBy(() -> service.saveGrpcEntity(message, wrongId))
                .withMessage("Awaiting an organization synchronization");
        assertEquals(1, LOGGING_EXTENSION.getEvents().size());
        var loggingEvent = LOGGING_EXTENSION.getEvents().get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo(OrganizationServiceImpl.class.getName());
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo(message);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    }
}
