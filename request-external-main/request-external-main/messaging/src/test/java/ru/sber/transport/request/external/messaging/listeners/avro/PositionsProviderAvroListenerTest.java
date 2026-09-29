package ru.sber.transport.request.external.messaging.listeners.avro;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
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
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.messages.corporate.avro.PositionMessage;
import ru.sber.transport.request.external.model.Position;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных должностей из кафки авро")
class PositionsProviderAvroListenerTest {

    private final PositionsProvider positionsProvider = mock(PositionsProvider.class);

    private final Consumer<Message<PositionMessage>> input = new PositionAvroListener(positionsProvider);

    @Test
    @DisplayName("Получение")
    void test() {
        final var message = PositionMessage.newBuilder()
                .setHumanReadableId(Instancio.create(String.class))
                .setName(Instancio.create(String.class))
                .setId(UUID.randomUUID())
                .setActive(Instancio.create(Boolean.class))
                .setOrganizationId(UUID.randomUUID())
                .setSelfApproved(Instancio.create(Boolean.class))
                .setAvailableClasses(Instancio.createList(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Position.class);

        verify(positionsProvider).save(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(message.getId());
            it.assertThat(actual.getAvailableClasses()).isEqualTo(message.getAvailableClasses());
        });
    }

}