package ru.sber.transport.request.external.messaging.listeners.avro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import java.util.UUID;
import java.util.function.Consumer;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных подразделений из кафки авро")
class DepartmentsProviderAvroListenerTest {

    private final DepartmentsProvider departmentsProvider = mock(DepartmentsProvider.class);

    private final Consumer<Message<DepartmentMessage>> input = new DepartmentAvroListener(departmentsProvider);

    @Test
    @DisplayName("Получение подразделений")
    void test() {
        final var message = DepartmentMessage.newBuilder()
                .setHumanReadableId(Instancio.create(String.class))
                .setCode(Instancio.create(String.class))
                .setDeleted(Instancio.create(Boolean.class))
                .setHeadId(UUID.randomUUID())
                .setId(UUID.randomUUID())
                .setOrganizationId(UUID.randomUUID())
                .setName(Instancio.create(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Department.class);

        verify(departmentsProvider).save(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHeadId()).isEqualTo(message.getHeadId());
        assertThat(actual.getParentId()).isEqualTo(message.getParentId());
        assertThat(actual.getName()).isEqualTo(message.getName());
        assertThat(actual.getStatus()).isEqualTo(message.getDeleted()? DepartmentStatus.INACTIVE : DepartmentStatus.ACTIVE);
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
        assertThat(actual.getLevel()).isEqualTo(1);
    }

}