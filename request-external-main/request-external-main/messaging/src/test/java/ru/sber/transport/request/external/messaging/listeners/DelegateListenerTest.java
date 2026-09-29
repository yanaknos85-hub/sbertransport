package ru.sber.transport.request.external.messaging.listeners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import java.util.function.Consumer;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.request.external.model.Delegate;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных делегатов из кафки")
class DelegateListenerTest {

    private final DelegatesProvider delegatesProvider = mock(DelegatesProvider.class);

    private final Consumer<Message<DelegateMessage>> consumer = new DelegateListener(delegatesProvider);

    @Test
    @DisplayName("Проверка получения данных делегатов из кафки")
    void test_received() {
        final var message = Instancio.of(DelegateMessage.class)
                .set(Select.field(DelegateMessage::getTransportTypeId), TransportTypeEnum.TAXI.getId())
                .create();

        final var rawMessage = MessageBuilder.withPayload(message).build();

        consumer.accept(rawMessage);

        final var dataCaptor = ArgumentCaptor.forClass(Delegate.class);

        verify(delegatesProvider).save(dataCaptor.capture());
    }

    @Test
    @DisplayName("Проверка получения данных делегатов из кафки. Не тот тип")
    void test_wrongType() {
        final var message = Instancio.of(DelegateMessage.class)
                .set(Select.field(DelegateMessage::getTransportTypeId), TransportTypeEnum.BICYCLE.getId())
                .create();

        final var rawMessage = MessageBuilder.withPayload(message).build();

        consumer.accept(rawMessage);

        final var dataCaptor = ArgumentCaptor.forClass(Delegate.class);

        verify(delegatesProvider, never()).save(dataCaptor.capture());
    }

}