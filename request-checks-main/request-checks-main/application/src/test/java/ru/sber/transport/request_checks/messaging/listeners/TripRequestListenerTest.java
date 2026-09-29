package ru.sber.transport.request_checks.messaging.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.service.TripRequestService;

@Isolated
@DisplayName("Проверка получения данных о заявке на поездку из кафки (топик заявок)")
class TripRequestListenerTest {

    private final TripRequestService tripRequestService = mock(TripRequestService.class);

    private final Consumer<Message<RequestMessage>> input = new TripRequestListener(tripRequestService);

    @Test
    @DisplayName("Получение заявок на поездку")
    void shouldProcessRequestMessageAndCallService() {
        val waypoints = Instancio.ofList(RequestMessage.Waypoint.class)
            .size(2)
            .create();
        val message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .waypoints(waypoints)
            .passenger(Instancio.create(RequestMessage.Employee.class))
            .transportType("PUBLIC")
            .expected(Instancio.create(RequestMessage.ExpectedData.class))
            .desiredDate(Instancio.create(LocalDateTime.class))
            .approvalDate(Instancio.create(LocalDateTime.class))
            .approvalId(UUID.randomUUID())
            .transportCompensation(List.of(Instancio.create(RequestMessage.TransportCompensation.class)))
            .timeZone("+3:00")
            .build();

        input.accept(MessageBuilder.withPayload(message).build());

        val messageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        verify(tripRequestService).saveFromRequestMessage(messageCaptor.capture());

        val actual = messageCaptor.getValue();

        assertThat(actual).isNotNull();
        assertThat(actual.getWaypoints()).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP_TRANSFER", "PUBLIC", "PERSONAL", "CARSHARING", "TAXI"})
    @DisplayName("Обработка заявок с валидными типами транспорта (верхний регистр)")
    void shouldProcessValidTransportTypes(String transportType) {
        val waypoints = Instancio.ofList(RequestMessage.Waypoint.class)
            .size(2)
            .create();
        val message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .waypoints(waypoints)
            .passenger(Instancio.create(RequestMessage.Employee.class))
            .transportType(transportType)
            .expected(Instancio.create(RequestMessage.ExpectedData.class))
            .desiredDate(Instancio.create(LocalDateTime.class))
            .approvalDate(Instancio.create(LocalDateTime.class))
            .approvalId(UUID.randomUUID())
            .transportCompensation(List.of(Instancio.create(RequestMessage.TransportCompensation.class)))
            .timeZone("+3:00")
            .build();

        input.accept(MessageBuilder.withPayload(message).build());

        val messageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        verify(tripRequestService).saveFromRequestMessage(messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isNotNull();
        assertThat(messageCaptor.getValue().getTransportType()).isEqualTo(transportType);
    }

    @Test
    @DisplayName("Отклонение заявки с недопустимым типом транспорта")
    void shouldRejectInvalidTransportType() {
        val waypoints = Instancio.ofList(RequestMessage.Waypoint.class)
            .size(2)
            .create();
        val message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .waypoints(waypoints)
            .passenger(Instancio.create(RequestMessage.Employee.class))
            .transportType("INVALID_TYPE")
            .expected(Instancio.create(RequestMessage.ExpectedData.class))
            .desiredDate(Instancio.create(LocalDateTime.class))
            .approvalDate(Instancio.create(LocalDateTime.class))
            .approvalId(UUID.randomUUID())
            .transportCompensation(List.of(Instancio.create(RequestMessage.TransportCompensation.class)))
            .timeZone("+3:00")
            .build();

        input.accept(MessageBuilder.withPayload(message).build());

        verify(tripRequestService, never()).saveFromRequestMessage(any());
    }

    @Test
    @DisplayName("Отклонение заявки с null транспортным типом")
    void shouldRejectNullTransportType() {
        val waypoints = Instancio.ofList(RequestMessage.Waypoint.class)
            .size(2)
            .create();
        val message = RequestMessage.builder()
            .id(UUID.randomUUID())
            .waypoints(waypoints)
            .passenger(Instancio.create(RequestMessage.Employee.class))
            .transportType(null)
            .expected(Instancio.create(RequestMessage.ExpectedData.class))
            .desiredDate(Instancio.create(LocalDateTime.class))
            .approvalDate(Instancio.create(LocalDateTime.class))
            .approvalId(UUID.randomUUID())
            .transportCompensation(List.of(Instancio.create(RequestMessage.TransportCompensation.class)))
            .timeZone("+3:00")
            .build();

        input.accept(MessageBuilder.withPayload(message).build());

        verify(tripRequestService, never()).saveFromRequestMessage(any());
    }

}
