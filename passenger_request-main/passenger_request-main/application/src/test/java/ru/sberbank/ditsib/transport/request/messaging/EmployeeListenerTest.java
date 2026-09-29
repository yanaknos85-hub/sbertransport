package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.messaging.message.EmployeeMessage;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения сотрудников")
@MockitoBean(types = JwtDecoder.class)
class EmployeeListenerTest extends KafkaTest {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @MockitoSpyBean
    private EmployeeService employeeService;
    
    @Autowired
    @Qualifier("employeesInput")
    private Consumer<Message<EmployeeMessage>> employeesInput;
    
    private Department department;
    
    private UUID organizationId;
    private Long organizationDigitId;
    private String employeeHumanreadableid;
    
    
    private Position position;
    
    @AfterEach
    void dropRepository() {
        employeeRepository.deleteAllInBatch();
        employeeRepository.flush();
        positionRepository.deleteAll();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }
    
    @BeforeEach
    void fillRepository() {
        organizationId = UUID.randomUUID();
        organizationDigitId = 1L;
        employeeHumanreadableid = "US-0001-1";
        var organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());
        position =
                positionRepository
                        .save(Position.builder().id(UUID.randomUUID()).positionName("Boss")
                                      .organizationId(organization.getId())
                                      .build());
        
        
        department = departmentRepository
                .save(Department.builder()
                                .id(UUID.randomUUID())
                                .organization(organization)
                                .departmentName("Depname")
                                .build());
    }
    
    @Test
    @DisplayName("Новый пользователь")
    void handleEmployee_new() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(id)
                                     .departmentId(department.getId())
                                     .positionId(position.getId())
                                     .firstName("First")
                                     .lastName("Last").humanReadableId(employeeHumanreadableid).build();
        
        assertEquals(0, employeeRepository.count());
        
        employeesInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(employeeService).save(any(Employee.class));
        assertEquals(1, employeeRepository.count());
        assertEquals(id, employeeRepository.findAll().get(0).getId());
        assertThat(employeeRepository.findAll().get(0).getHumanReadableId()).isEqualTo(employeeHumanreadableid);
    }
    
    @Test
    @DisplayName("Редактировать пользователя")
    void handleEmployee_update() {
        // Create
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(id)
                                     .departmentId(department.getId())
                                     .positionId(position.getId())
                                     .firstName("First")
                                     .lastName("Last").humanReadableId(employeeHumanreadableid).build();
        
        assertEquals(0, employeeRepository.count());
        
        employeesInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(employeeService).save(any(Employee.class));
        assertEquals(1, employeeRepository.count());
        assertEquals(id, employeeRepository.findAll().get(0).getId());
        assertThat(employeeRepository.findAll().get(0).getHumanReadableId()).isEqualTo(employeeHumanreadableid);
        assertThat(employeeRepository.findAll().get(0).getFirstName()).isEqualTo("First");
        
        // Update
        message.setFirstName("Updated");
        employeesInput.accept(MessageBuilder.withPayload(message).build());
        verify(employeeService, times(2)).save(any(Employee.class));
        assertEquals(1, employeeRepository.count());
        assertEquals(id, employeeRepository.findAll().get(0).getId());
        assertThat(employeeRepository.findAll().get(0).getFirstName()).isEqualTo("Updated");
    }
    
    @Test
    @DisplayName("Удаление3")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        dropRepository();
        employeeRepository.save(Employee.builder().id(id)
                                        .firstName("First")
                                        .lastName("Last")
                                        .positionId(position.getId())
                                        .department(department)
                                        .humanReadableId(employeeHumanreadableid)
                                        .build());
        var message = EmployeeMessage.builder().id(id)
                                     .departmentId(department.getId())
                                     .firstName("First")
                                     .lastName("Last")
                                     .deleted(true)
                                     .build();
        
        assertEquals(1, employeeRepository.count());
        
        employeesInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(employeeService).get(any(UUID.class));
        verify(employeeService).delete(any(Employee.class));
        assertEquals(0, employeeRepository.findAllByActive(true).size());
    }
}