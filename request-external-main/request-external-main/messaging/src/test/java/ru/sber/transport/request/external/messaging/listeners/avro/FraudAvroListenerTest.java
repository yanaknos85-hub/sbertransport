package ru.sber.transport.request.external.messaging.listeners.avro;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.util.List;
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
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.messages.ai_receipt_scanner.avro.FraudMessage;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;
import ru.sber.transport.request.external.messaging.senders.FraudMonitoringSender;
import ru.sber.transport.request.external.model.Fraud;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка получения данных о фроде из кафки авро")
class FraudAvroListenerTest {

    private final FraudsProvider fraudsProvider = mock(FraudsProvider.class);
    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);
    private final FraudMonitoringSender fraudMonitoringSender = mock(FraudMonitoringSender.class);

    private final Consumer<Message<FraudMessage>> input = new FraudAvroListener(tripOrdersProvider, fraudsProvider);

    @Test
    @DisplayName("Получение актуальных данных о фроде из кафки")
    void testSaveFraudForExistingTripOrder() {
        when(tripOrdersProvider.exists(any())).thenReturn(true);
        final var message = FraudMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setComment(Instancio.create(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Fraud.class);

        verify(fraudsProvider).save(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(message.getId());
            it.assertThat(actual.getComment()).isEqualTo(message.getComment());
            it.assertThat(actual.getType()).isEqualTo("RECEIPT");
        });

        verifyNoInteractions(fraudMonitoringSender);
    }

    @Test
    @DisplayName("Получение данных о фроде из кафки для несуществующей заявки")
    void testSkipSavingFraudForNonExistingOrder() {
        when(tripOrdersProvider.exists(any())).thenReturn(false);
        final var message = FraudMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setComment(Instancio.create(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        verify(fraudsProvider, never()).save(any(Fraud.class));
        verifyNoInteractions(fraudMonitoringSender);
    }

}