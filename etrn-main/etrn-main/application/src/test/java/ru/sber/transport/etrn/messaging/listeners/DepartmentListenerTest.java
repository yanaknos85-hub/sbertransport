package ru.sber.transport.etrn.messaging.listeners;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import ru.sber.transport.etrn.database.dao.DepartmentRepository;
import ru.sber.transport.etrn.database.dao.EmployeeRepository;
import ru.sber.transport.etrn.database.dao.OrganizationRepository;
import ru.sber.transport.etrn.database.model.Department;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sber.transport.etrn.service.DepartmentService;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка слушателя подразделений")
class DepartmentListenerTest extends KafkaTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @SpyBean
    private DepartmentService departmentService;

    private Organization organization;

    private UUID departmentHeadId;

    private String departmentHumanreadableid;

    private Department department1;

    @BeforeEach
    void fillRepository() {
        departmentRepository.clearAll();
        var organizationId = UUID.randomUUID();
        var organizationDigitId = 1L;
        departmentHumanreadableid = "DT-0001-1";
        organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());

        department1 = departmentRepository
                .save(Department.builder()
                        .id(UUID.randomUUID())
                        .humanReadableId(departmentHumanreadableid)
                        .organizationId(organization.getId())
                        .departmentName("Depname")
                        .build());

        departmentHeadId = UUID.randomUUID();
        var employeeHumanReadableId = "US-0001-1";
        employeeRepository.save(Employee.builder().id(departmentHeadId)
                .humanReadableId(employeeHumanReadableId)
                .firstName("First")
                .lastName("Last")
                .department(department1)
                .build());

    }

    @AfterEach
    void dropRepository() {

        List<Department> all = departmentRepository.findAll();
        departmentRepository.saveAll(all);
        departmentRepository.saveAndFlush(department1);
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Новое")
    void handleOrganization_new() {

        var id = UUID.randomUUID();
        String depHumanReadableId = "DT-0001-2";
        var message = DepartmentMessage.builder()
                .code("Code")
                .departmentName("Name")
                .id(id)
                .humanReadableId(depHumanReadableId)
                .organizationId(organization.getId())
                .build();

        assertThat(departmentRepository.count()).isEqualTo(1);

        produceMessage("service.organization.department", message);

        verify(departmentService).save(any(Department.class));
        assertThat(departmentRepository.count()).isEqualTo(2);
        assertThat(departmentRepository.findAll().get(1).getId()).isEqualTo(id);

        assertThat(departmentRepository.findAll().get(1).getHumanReadableId()).isEqualTo(depHumanReadableId);
        assertThat(departmentRepository.findAll().get(0).getHumanReadableId()).isEqualTo(departmentHumanreadableid);
    }

    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        departmentRepository.save(Department.builder()
                .id(id)
                .organizationId(organization.getId())
                .departmentName("fgd")
                .build());
        var message = DepartmentMessage.builder()
                .code("Code")
                .departmentHeadId(UUID.randomUUID())
                .departmentName("Name")
                .id(id)
                .organizationId(organization.getId())
                .location("Location")
                .deleted(true)
                .build();
        assertThat(departmentRepository.findAllByActive(true)).hasSize(2);

        produceMessage("service.organization.department", message);

        verify(departmentService, atLeastOnce()).delete(any(Department.class));
        assertThat(departmentRepository.findAllByActive(true)).hasSize(1);
    }
}
