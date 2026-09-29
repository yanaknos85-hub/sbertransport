package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
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
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EmbeddedPostgres
@Transactional
@DisplayName("Получатель подразделений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class DepartmentListenerImplTest {

    @MockitoBean
    private DepartmentService service;

    @Autowired
    @Qualifier("departments")
    private Consumer<Message<DepartmentMessage>> departmentMessageInput;

    @DisplayName("Новый")
    @Test
    void test_new() {
        var message = DepartmentMessage.newBuilder()
                .setCode("Code")
                .setHeadId(UUID.randomUUID())
                .setDeleted(false)
                .setName("Name")
                .setId(UUID.randomUUID())
                .setHumanReadableId("HRI")
                .setLocation("Location")
                .setOrganizationId(UUID.randomUUID())
                .setParentId(UUID.randomUUID())
                .build();

        when(service.save(any(Department.class))).then(inv -> inv.getArgument(0));
        departmentMessageInput.accept(MessageBuilder.withPayload(message).build());

        verify(service, never()).delete(any(UUID.class));
    }

    @DisplayName("Удален")
    @Test
    void test_delete() {
        var message = DepartmentMessage.newBuilder()
                .setCode("Code")
                .setHeadId(UUID.randomUUID())
                .setDeleted(true)
                .setName("Name")
                .setId(UUID.randomUUID())
                .setHumanReadableId("HRI")
                .setLocation("Location")
                .setOrganizationId(UUID.randomUUID())
                .setParentId(UUID.randomUUID()).build();

        departmentMessageInput.accept(MessageBuilder.withPayload(message).build());
        var idCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(service, never()).save(any(Department.class));
        verify(service).delete(idCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(message.getId());
    }

}