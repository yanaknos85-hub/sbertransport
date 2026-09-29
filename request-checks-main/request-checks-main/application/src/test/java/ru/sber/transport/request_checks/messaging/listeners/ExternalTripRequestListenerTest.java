package ru.sber.transport.request_checks.messaging.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;
import ru.sber.transport.request_checks.service.TripRequestService;

@Isolated
@DisplayName("Проверка получения данных о заявке на поездку из кафки (топик внешних заявок)")
class ExternalTripRequestListenerTest {

    private final TripRequestService tripRequestService = mock(TripRequestService.class);

    private final Consumer<Message<ExternalRequestMessage>> input = new ExternalTripRequestListener(tripRequestService);

    @Test
    @DisplayName("Получение заявок на поездку (Яндекс Go)")
    void shouldProcessExternalMessageAndCallService() {
        val startWaypoint = ExternalRequestMessage.WaypointMessage.builder()
            .id(UUID.randomUUID())
            .country(Instancio.create(String.class))
            .region(Instancio.create(String.class))
            .city(Instancio.create(String.class))
            .street(Instancio.create(String.class))
            .house(Instancio.create(String.class))
            .structure(Instancio.create(String.class))
            .building(Instancio.create(String.class))
            .longitude(Instancio.create(Double.class))
            .latitude(Instancio.create(Double.class))
            .build();

        val endWaypoint = ExternalRequestMessage.WaypointMessage.builder()
            .id(UUID.randomUUID())
            .country(Instancio.create(String.class))
            .region(Instancio.create(String.class))
            .city(Instancio.create(String.class))
            .street(Instancio.create(String.class))
            .house(Instancio.create(String.class))
            .structure(Instancio.create(String.class))
            .building(Instancio.create(String.class))
            .longitude(Instancio.create(Double.class))
            .latitude(Instancio.create(Double.class))
            .build();

        val message = Instancio.of(ExternalRequestMessage.class)
            .set(field(ExternalRequestMessage::getWaypoints), List.of(startWaypoint, endWaypoint))
            .create();

        input.accept(MessageBuilder.withPayload(message).build());

        val messageCaptor = ArgumentCaptor.forClass(ExternalRequestMessage.class);
        verify(tripRequestService).saveFromExternalMessage(messageCaptor.capture());

        val actual = messageCaptor.getValue();

        assertThat(actual).isNotNull();
        assertThat(actual.getWaypoints()).hasSameSizeAs(message.getWaypoints());
    }

}
