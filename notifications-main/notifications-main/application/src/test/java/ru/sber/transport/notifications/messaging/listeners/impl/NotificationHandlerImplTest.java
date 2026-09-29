package ru.sber.transport.notifications.messaging.listeners.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.messaging.message.NotificationMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@DisplayName("Получатель уведомлений request_external")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class NotificationHandlerImplTest {

    @Autowired
    @Qualifier("notificationInput")
    private Consumer<Message<NotificationMessage>> notificationMessageInput;
    @Autowired
    private NotificationRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    @Transactional
    @Sql(scripts = "/sql/settings.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/employee.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/truncate_notification.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void accept() {
        var id = UUID.randomUUID();
        var receiver = UUID.fromString("582a9628-7980-46f0-9264-f7d3699f9e22");
        var data = List.of(
                new NotificationMessage.Data("humanReadableId", "YA-0001-00000001"),
                new NotificationMessage.Data("passengerName", "Петр Михайлович Р."),
                new NotificationMessage.Data("approverName", "Петр Михайлович Р."),
                new NotificationMessage.Data("requestId", id.toString())
        );
        var message = new NotificationMessage(id, List.of(receiver), "APPROVE", "SBT", data);


        notificationMessageInput.accept(MessageBuilder.withPayload(message).build());

        var saved = repository.findByEntityId(id);
        assertThat(saved).isPresent();
        var result = saved.get();
        var expectedEntity = objectMapper.writeValueAsString(message.data()
                .stream()
                .collect(Collectors.toMap(NotificationMessage.Data::key, NotificationMessage.Data::value)));
        assertThat(result.getSettings().getId()).isEqualTo(UUID.fromString("715f8ff7-3bd0-4afc-ad4b-0de6136a77d4"));
        assertThat(result.isSent()).isFalse();
        assertThat(result.getReceiverId()).isEqualTo(receiver);
        assertThat(result.getEntity()).isEqualTo(expectedEntity);
    }
}