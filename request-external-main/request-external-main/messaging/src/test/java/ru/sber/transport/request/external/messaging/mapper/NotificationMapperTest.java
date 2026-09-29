package ru.sber.transport.request.external.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.qameta.allure.Feature;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.request.external.messaging.TestEmployee;
import ru.sber.transport.request.external.messaging.TestOrder;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка маппера сообщений нотификации")
class NotificationMapperTest {

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);
    private final NotificationMapper mapper = new NotificationMapper(employeesProvider);

    @Test
    void toMessage() {
        final var order = Instancio.of(TestOrder.class)
                .set(Select.field(TestOrder::getPassenger),
                        new TestEmployee(UUID.randomUUID(), "Петр", "Романов", "Михайлович",
                                null, null, null, null, null))
                .create();
        final var messageType = Instancio.of(String.class).create();
        final var receiverId = UUID.randomUUID();

        var result = mapper.toMessage(order, messageType, receiverId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getMessageType()).isEqualTo(messageType);
        assertThat(result.getApplicationType()).isEqualTo("SBT");
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).hasSize(3);
        assertThat(result.getData().get(0).getKey()).isEqualTo("humanReadableId");
        assertThat(result.getData().get(0).getValue()).isEqualTo(order.getHumanReadableId());
        assertThat(result.getData().get(1).getKey()).isEqualTo("passengerName");
        assertThat(result.getData().get(1).getValue()).isEqualTo("Петр Михайлович Р.");
        assertThat(result.getData().get(2).getKey()).isEqualTo("requestId");
        assertThat(result.getData().get(2).getValue()).isEqualTo(String.valueOf(order.getId()));
        assertThat(result.getReceivers()).hasSize(1);
        assertThat(result.getReceivers().get(0)).isEqualTo(receiverId);
    }

    @Test
    void toAvroMessage() {
        final var order = Instancio.of(TestOrder.class)
                .set(Select.field(TestOrder::getPassenger),
                        new TestEmployee(UUID.randomUUID(), "Петр", "Романов", "Михайлович",
                                null, null, null, null, null))
                .create();
        final var messageType = Instancio.of(String.class).create();
        final var receiverId = UUID.randomUUID();

        var result = mapper.toAvroMessage(order, messageType, receiverId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getMessageType()).isEqualTo(messageType);
        assertThat(result.getApplicationType()).isEqualTo("SBT");
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getValues()).isNotNull();
        assertThat(result.getData().getValues()).hasSize(3);
        assertThat(result.getData().getValues().get(0).getKey()).isEqualTo("humanReadableId");
        assertThat(result.getData().getValues().get(0).getValue()).isEqualTo(order.getHumanReadableId());
        assertThat(result.getData().getValues().get(1).getKey()).isEqualTo("passengerName");
        assertThat(result.getData().getValues().get(1).getValue()).isEqualTo("Петр Михайлович Р.");
        assertThat(result.getData().getValues().get(2).getKey()).isEqualTo("requestId");
        assertThat(result.getData().getValues().get(2).getValue()).isEqualTo(String.valueOf(order.getId()));
        assertThat(result.getReceivers()).hasSize(1);
        assertThat(result.getReceivers().get(0)).isEqualTo(receiverId);
    }
}