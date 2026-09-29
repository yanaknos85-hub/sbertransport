package ru.sberbank.ditsib.messaging.listener;

import io.qameta.allure.Feature;
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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.database.dao.PositionRepository;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.database.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest
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

    @Autowired
    @Qualifier("employeesInput")
    private Consumer<Message<EmployeeMessage>> employeesInput;

    @Test
    @DisplayName("Создать, редактировать, удалить пользователя")
    void handleEmployee() {
        var organizationId = UUID.randomUUID();
        var organizationDigitId = 1L;
        var employeeHumanreadableId = "US-0001-1";
        var organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());
        var position = positionRepository.save(Position.builder().id(UUID.randomUUID()).positionName("Boss")
                .organization(organization)
                .build());

        var department = departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .humanReadableId("DT-0005-2")
                .departmentName("Depname")
                .build());

        var employeeId = UUID.randomUUID();
        var message = EmployeeMessage.builder().id(employeeId)
                .departmentId(department.getId())
                .organizationId(organizationId)
                .positionId(position.getId())
                .personnelNumber("123455")
                .userId(employeeId)
                .firstName("First")
                .lastName("Last").humanReadableId(employeeHumanreadableId).build();

        employeesInput.accept(MessageBuilder.withPayload(message).build());

        var savedRes = employeeRepository.findAll();
        assertThat(savedRes).hasSize(1);
        var savedEmployee = savedRes.get(0);
        assertThat(savedEmployee.getId()).isEqualTo(employeeId);
        assertThat(savedEmployee.getHumanReadableId()).isEqualTo(employeeHumanreadableId);
        assertThat(savedEmployee.getFirstName()).isEqualTo("First");

        var message2 = EmployeeMessage.builder().id(employeeId)
                .departmentId(department.getId())
                .organizationId(organizationId)
                .positionId(position.getId())
                .personnelNumber("123455")
                .userId(employeeId)
                .firstName("Updated")
                .lastName("Last").humanReadableId(employeeHumanreadableId).build();
        employeesInput.accept(MessageBuilder.withPayload(message2).build());

        var updatedRes = employeeRepository.findAll();
        assertThat(updatedRes).hasSize(1);
        var updatedEmployee = updatedRes.get(0);
        assertThat(updatedEmployee.getId()).isEqualTo(employeeId);
        assertThat(updatedEmployee.getFirstName()).isEqualTo("Updated");

        var message3 = EmployeeMessage.builder().id(employeeId)
                .departmentId(department.getId())
                .firstName("Updated")
                .organizationId(organizationId)
                .positionId(position.getId())
                .personnelNumber("123455")
                .userId(employeeId)
                .lastName("Last")
                .deleted(true)
                .build();

        employeesInput.accept(MessageBuilder.withPayload(message3).build());

        var deletedRes = employeeRepository.findAll().stream().filter(Employee::isActive).toList();
        assertThat(deletedRes).isEmpty();
    }
}