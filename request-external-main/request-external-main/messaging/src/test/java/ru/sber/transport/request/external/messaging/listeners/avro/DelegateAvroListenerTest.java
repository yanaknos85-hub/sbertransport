package ru.sber.transport.request.external.messaging.listeners.avro;

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
import ru.sber.transport.messages.corporate.avro.DelegateData;
import ru.sber.transport.request.external.model.Delegate;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных делегатов из кафки авро")
class DelegateAvroListenerTest {

    private final DelegatesProvider delegatesProvider = mock(DelegatesProvider.class);

    private final Consumer<Message<DelegateData>> consumer = new DelegateAvroListener(delegatesProvider);

    @Test
    @DisplayName("Проверка получения данных делегатов из кафки")
    void test_received() {
        final var message = Instancio.of(DelegateData.class)
                .set(Select.field(DelegateData::getTransportType), "TAXI")
                .create();

        final var rawMessage = MessageBuilder.withPayload(message).build();

        consumer.accept(rawMessage);

        final var dataCaptor = ArgumentCaptor.forClass(Delegate.class);

        verify(delegatesProvider).save(dataCaptor.capture());
    }

    @Test
    @DisplayName("Проверка получения данных делегатов из кафки. Не тот тип")
    void test_wrongType() {
        final var message = Instancio.of(DelegateData.class)
                .set(Select.field(DelegateData::getTransportType), "WRONG")
                .create();

        final var rawMessage = MessageBuilder.withPayload(message).build();

        consumer.accept(rawMessage);

        final var dataCaptor = ArgumentCaptor.forClass(Delegate.class);

        verify(delegatesProvider, never()).save(dataCaptor.capture());
    }

}