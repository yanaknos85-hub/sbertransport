package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messages.corporate.avro.Contact;
import ru.sber.transport.messages.corporate.avro.ContactEmployeeType;
import ru.sber.transport.messages.corporate.avro.EmployeeType;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@EmbeddedPostgres
@DisplayName("Получатель сотрудников")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class EmployeeListenerImplTest {

    @MockitoBean
    private EmployeeService service;

    @Autowired
    @Qualifier("employee")
    private Consumer<Message<EmployeeMessage>> employeeMessageInput;

    @DisplayName("Новый")
    @Test
    void test_new() {
        var message = EmployeeMessage.newBuilder()
                .setLastName("Last name")
                .setFirstName("First name")
                .setDeleted(false)
                .setPatronymic("Patronymic")
                .setId(UUID.randomUUID())
                .setContacts(List.of(
                        Contact.newBuilder().setInternal(true).setIsConfirmed(false).setType(ContactEmployeeType.EMAIL).setValue("Email").build(),
                        Contact.newBuilder().setInternal(true).setIsConfirmed(true).setType(ContactEmployeeType.MOBILE).setValue("Mobile").build()
                ))
                .setHumanReadableId("HRI")
                .setPersonnelNumber(Instancio.create(String.class))
                .setOrganizationId(UUID.randomUUID())
                .setDepartmentId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setConsent(Instancio.create(Boolean.class))
                .setUserId(UUID.randomUUID())
                .build();

        employeeMessageInput.accept(MessageBuilder.withPayload(message).build());

        var captor = ArgumentCaptor.forClass(Employee.class);

        verify(service).save(captor.capture());
        verify(service, never()).delete(any(UUID.class));

        var actual = captor.getValue();

        assertThat(actual)
                .matches(act -> act.getPatronymic().equals(message.getPatronymic()), "Patronymic")
                .matches(act -> act.getFirstName().equals(message.getFirstName()), "First name")
                .matches(act -> act.getLastName().equals(message.getLastName()), "Last name")
                .matches(act -> act.getEmail().equals(message.getContacts().getFirst().getValue()), "Email")
                .matches(act -> act.getDepartmentId().equals(message.getDepartmentId()), "Department ID")
                .matches(act -> act.getUserId().equals(message.getUserId()), "User ID")
                .matches(act -> act.getId().equals(message.getId()), "ID");
    }

    @DisplayName("Удален")
    @Test
    void test_delete() {
        var message = EmployeeMessage.newBuilder()
                .setLastName("Last name")
                .setFirstName("First name")
                .setDeleted(true)
                .setPatronymic("Patronymic")
                .setId(UUID.randomUUID())
                .setDepartmentId(UUID.randomUUID())
                .setPositionId(UUID.randomUUID())
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setConsent(Instancio.create(Boolean.class))
                .setContacts(List.of(
                        Contact.newBuilder().setInternal(true).setIsConfirmed(false).setType(ContactEmployeeType.EMAIL).setValue("Email").build(),
                        Contact.newBuilder().setInternal(true).setIsConfirmed(true).setType(ContactEmployeeType.MOBILE).setValue("Mobile").build()
                ))
                .setHumanReadableId("HRI")
                .setPersonnelNumber(Instancio.create(String.class))
                .setOrganizationId(UUID.randomUUID())
                .setUserId(UUID.randomUUID())
                .build();

        employeeMessageInput.accept(MessageBuilder.withPayload(message).build());

        var idCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(service, never()).save(any(Employee.class));
        verify(service).delete(idCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(message.getId());
    }

}