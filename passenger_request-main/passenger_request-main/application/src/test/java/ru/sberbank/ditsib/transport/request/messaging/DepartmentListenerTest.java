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
import ru.sberbank.ditsib.transport.request.messaging.message.DepartmentMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("OptionalGetWithoutIsPresent")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения подразделений")
@MockitoBean(types = JwtDecoder.class)
class DepartmentListenerTest extends KafkaTest {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @MockitoSpyBean
    private DepartmentService departmentService;
    
    @Autowired
    @Qualifier("departmentsInput")
    private Consumer<Message<DepartmentMessage>> departmentsInput;
    
    private Organization organization;
    private Position position;
    
    private UUID departmentId;
    private UUID departmentHeadId;
    private String employeeHumanReadableId;
    private String departmentHumanreadableid;
    
    private Department department1;
    
    @AfterEach
    void dropRepository() {
        
        List<Department> all = departmentRepository.findAll();
        all.forEach(elt -> elt.setDepartmentHead(null));
        departmentRepository.saveAll(all);
        departmentRepository.saveAndFlush(department1);
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }
    
    @BeforeEach
    void fillRepository() {
        departmentRepository.clearAll();
        var organizationId = UUID.randomUUID();
        var organizationDigitId = 1L;
        departmentHumanreadableid = "DT-0001-1";
        organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());
        
        position = positionRepository.save(
                Position.builder().id(UUID.randomUUID())
                        .positionName("Boss").organizationId(organization.getId())
                        .build());
        
        
        department1 = departmentRepository
                .save(Department.builder()
                                .id(UUID.randomUUID())
                                .humanReadableId(departmentHumanreadableid)
                                .organization(organization)
                                .departmentName("Depname")
                                .build());
        
        departmentHeadId = UUID.randomUUID();
        employeeHumanReadableId = "US-0001-1";
        employeeRepository.save(Employee.builder().id(departmentHeadId)
                                        .humanReadableId(employeeHumanReadableId)
                                        .firstName("First")
                                        .lastName("Last")
                                        .positionId(position.getId())
                                        .department(department1)
                                        .build());
        
    }
    
    @Test
    @DisplayName("Новое")
    void handleOrganization_new() {
        
        var id = UUID.randomUUID();
        String depHumanReadableId = "DT-0001-2";
        var message = DepartmentMessage.builder()
                                       .code("Code")
                                       .departmentHeadId(departmentHeadId)
                                       .departmentName("Name")
                                       .id(id)
                                       .humanReadableId(depHumanReadableId)
                                       .organizationId(organization.getId())
                                       .location("Location")
                                       .parentId(departmentId)
                                       .build();
        
        assertEquals(1, departmentRepository.count());
        
        departmentsInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(departmentService).save(any(Department.class));
        Department fromMessage = departmentRepository.findById(id).get();
        assertEquals(departmentHeadId, fromMessage.getDepartmentHead());
        assertEquals("Location", fromMessage.getLocation());
        assertEquals(2, departmentRepository.count());
        assertEquals(id, departmentRepository.findAll().get(1).getId());
        
        assertEquals(depHumanReadableId, departmentRepository.findAll().get(1).getHumanReadableId());
        assertThat(departmentRepository.findAll().get(0).getHumanReadableId()).isEqualTo(departmentHumanreadableid);
    }
    
    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        departmentRepository.save(Department.builder()
                                            .id(id)
                                            .organization(organization)
                                            .departmentName("fgd")
                                            .build());
        var message = DepartmentMessage.builder()
                                       .code("Code")
                                       .departmentHeadId(UUID.randomUUID())
                                       .departmentName("Name")
                                       .id(id)
                                       .organizationId(organization.getId())
                                       .location("Location")
                                       .parentId(departmentId)
                                       .deleted(true)
                                       .build();
        assertEquals(2, departmentRepository.findAllByActive(true).size());
        
        departmentsInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(departmentService, atLeastOnce()).get(any());
        verify(departmentService, atLeastOnce()).delete(any(Department.class));
        assertEquals(1, departmentRepository.findAllByActive(true).size());
    }
}