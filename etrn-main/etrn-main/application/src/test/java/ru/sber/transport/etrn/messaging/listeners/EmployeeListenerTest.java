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
import ru.sber.transport.etrn.service.EmployeeService;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка слушателя сотрудников")
class EmployeeListenerTest extends KafkaTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @SpyBean
    private EmployeeService employeeService;

    private Department department;

    private String employeeHumanreadableid;

    @BeforeEach
    void init() {
        var organizationId = UUID.randomUUID();
        var organizationDigitId = 1L;
        employeeHumanreadableid = "US-0001-1";
        var organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());

        department = departmentRepository
                .save(Department.builder()
                        .id(UUID.randomUUID())
                        .organizationId(organization.getId())
                        .departmentName("Depname")
                        .build());
    }

    @AfterEach
    void dropRepository() {
        employeeRepository.deleteAllInBatch();
        employeeRepository.flush();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Новый пользователь")
    void test1() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(id)
                .departmentId(department.getId())
                .firstName("First")
                .lastName("Last").humanReadableId(employeeHumanreadableid).build();

        assertThat(employeeRepository.count()).isZero();

        produceMessage("service.organization.employee", message);

        verify(employeeService).save(any(Employee.class));
        assertThat(employeeRepository.count()).isEqualTo(1);
        assertThat(employeeRepository.findAll().getFirst().getId()).isEqualTo(id);
        assertThat(employeeRepository.findAll().getFirst().getHumanReadableId()).isEqualTo(employeeHumanreadableid);
    }

    @Test
    @DisplayName("Редактировать пользователя")
    void test2() {
        var id = UUID.randomUUID();
        var message = EmployeeMessage.builder()
                .id(id)
                .departmentId(department.getId())
                .firstName("First")
                .lastName("Last")
                .humanReadableId(employeeHumanreadableid)
                .build();

        assertThat(employeeRepository.count()).isZero();

        produceMessage("service.organization.employee", message);

        verify(employeeService).save(any(Employee.class));
        assertThat(employeeRepository.count()).isEqualTo(1);
        assertThat(employeeRepository.findAll().getFirst().getId()).isEqualTo(id);
        assertThat(employeeRepository.findAll().getFirst().getHumanReadableId()).isEqualTo(employeeHumanreadableid);
        assertThat(employeeRepository.findAll().getFirst().getFirstName()).isEqualTo("First");

        message = EmployeeMessage.builder()
                .id(id)
                .departmentId(department.getId())
                .firstName("Updated")
                .lastName("Last")
                .humanReadableId(employeeHumanreadableid)
                .build();

        produceMessage("service.organization.employee", message);
        verify(employeeService, times(2)).save(any(Employee.class));
        assertThat(employeeRepository.count()).isEqualTo(1);
        assertThat(employeeRepository.findAll().get(0).getId()).isEqualTo(id);
        assertThat(employeeRepository.findAll().get(0).getFirstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Удаление")
    void test3() {
        var id = UUID.randomUUID();
        employeeRepository.save(Employee.builder().id(id)
                .firstName("First")
                .lastName("Last")
                .department(department)
                .humanReadableId(employeeHumanreadableid)
                .build());
        var message = EmployeeMessage.builder().id(id)
                .departmentId(department.getId())
                .firstName("First")
                .lastName("Last")
                .deleted(true)
                .build();

        assertThat(employeeRepository.count()).isEqualTo(1);

        produceMessage("service.organization.employee", message);

        verify(employeeService).findById(any(UUID.class));
        verify(employeeService).delete(any(Employee.class));
        assertThat(employeeRepository.findAllByActive(true)).isEmpty();
    }
}
