package ru.sber.transport.notifications.messaging.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.mapper.TripMapperImpl;
import ru.sber.transport.notifications.messaging.message.NotificationMessage;
import ru.sber.transport.notifications.messaging.message.TripMessage;
import ru.sber.transport.notifications.services.Processor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Проверка слушателей")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Получение поездки")
    void test_consume_trip() throws JsonProcessingException {
        var processor = mock(Processor.class);
        var tripMapper = new TripMapperImpl();

        //noinspection unchecked
        var input = config.tripInput(processor, tripMapper);

        var message = Instancio.create(TripMessage.class);
        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        //noinspection unchecked
        input.accept(rawMessage);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);

        //noinspection unchecked
        verify(processor).process(tripCaptor.capture());
        var actual = tripCaptor.getValue();

        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHumanReadableId()).isEqualTo("TR-%04d-%08d".formatted(message.getContractorDigitId(), message.getDigitId()));
        assertThat(actual.getEndTime()).isEqualTo(message.getEndTime());
        assertThat(actual.getStartTime()).isEqualTo(message.getStartTime());
        assertThat(actual.getStatus()).isEqualTo(message.getStatus());
        assertThat(actual.getDriver().getId()).isEqualTo(message.getDriverId());
        assertThat(actual.getRequests()).hasSameElementsAs(message.getRequests());
        assertThat(actual.getWaypoints()).hasSameElementsAs(message.getWaypoints());
    }

    @Test
    @DisplayName("Получение уведомления")
    void test_notification() {
        final var handler = mock(NotificationHandler.class);

        final var input = config.notificationInput(handler);
        final var message = Instancio.create(NotificationMessage.class);
        final var rawMessage = MessageBuilder.withPayload(message).build();

        input.accept(rawMessage);

        final var notificationCaptor = ArgumentCaptor.forClass(NotificationMessage.class);

        verify(handler).accept(notificationCaptor.capture());

        final var actual = notificationCaptor.getValue();
        assertSoftly(it -> {
           it.assertThat(actual.getId()).isEqualTo(message.getId());
           it.assertThat(actual.applicationType()).isEqualTo(message.applicationType());
           it.assertThat(actual.messageType()).isEqualTo(message.messageType());
           it.assertThat(actual.data()).isEqualTo(message.data());
           it.assertThat(actual.receivers()).isEqualTo(message.receivers());
        });
    }

}
