package ru.sberbank.ditsib.messaging.listener;

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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.AggregationApplication;
import ru.sberbank.ditsib.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@EmbeddedPostgres
@SpringBootTest(classes = AggregationApplication.class)
@DisplayName("Проверка получения подразделений")
@MockitoBean(types = JwtDecoder.class)
class DepartmentListenerTest extends KafkaTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    @Qualifier("departmentsInput")
    private Consumer<Message<DepartmentMessage>> departmentsInput;

    private Organization organization;

    @BeforeEach
    void fillRepository() {
        departmentRepository.deleteAll();
        var organizationId = UUID.randomUUID();
        var organizationDigitId = 1L;
        organization = organizationRepository
                .save(Organization.builder().id(organizationId).digitId(organizationDigitId).build());
    }

    @Test
    @DisplayName("Новое")
    void handleDepartment_new() {
        var id = UUID.randomUUID();
        var depHumanReadableId = "DT-0001-2";
        var message = DepartmentMessage.builder()
                .code("Code")
                .departmentName("Name")
                .id(id)
                .humanReadableId(depHumanReadableId)
                .organizationId(organization.getId())
                .location("Location")
                .build();

        departmentsInput.accept(MessageBuilder.withPayload(message).build());

        var deps = departmentRepository.findAll();
        assertThat(deps).hasSize(1);
        var department = deps.get(0);
        assertThat(department.getId()).isEqualTo(id);

        assertThat(department.getHumanReadableId()).isEqualTo(depHumanReadableId);
    }

    @Test
    @DisplayName("Удаление")
    void handleDepartment_delete() {
        var id = UUID.randomUUID();
        var depHumanReadableId = "DT-0001-3";
        departmentRepository.save(Department.builder()
                .id(id)
                .humanReadableId(depHumanReadableId)
                .organization(organization)
                .departmentName("Department")
                .active(true)
                .build());
        var message = DepartmentMessage.builder()
                .code("Code")
                .humanReadableId(depHumanReadableId)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("Name")
                .id(id)
                .organizationId(organization.getId())
                .location("Location")
                .deleted(true)
                .build();

        departmentsInput.accept(MessageBuilder.withPayload(message).build());

        var res = departmentRepository.findAll().stream().filter(Department::isActive).toList();
        assertThat(res).isEmpty();
    }
}