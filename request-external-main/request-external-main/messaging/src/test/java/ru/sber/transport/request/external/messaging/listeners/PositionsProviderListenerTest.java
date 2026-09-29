package ru.sber.transport.request.external.messaging.listeners;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
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
import ru.sber.transport.request.external.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных должностей из кафки")
class PositionsProviderListenerTest {

    private final PositionsProvider positionsProvider = mock(PositionsProvider.class);

    private final Consumer<Message<PositionMessage>> input = new PositionListener(positionsProvider);

    @Test
    @DisplayName("Получение")
    void test() {
        final var message = Instancio.create(PositionMessage.class);

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Position.class);

        verify(positionsProvider).save(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(message.getId());
            it.assertThat(actual.getAvailableClasses()).isEqualTo(message.getAvailableClasses().stream().toList());
        });
    }

}