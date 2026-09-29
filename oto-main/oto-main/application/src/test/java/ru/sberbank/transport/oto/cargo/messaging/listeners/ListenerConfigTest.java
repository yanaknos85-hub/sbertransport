package ru.sberbank.transport.oto.cargo.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.messaging.messages.trip.DeadlineStateUpdateMessage;
import ru.sberbank.transport.oto.cargo.database.dao.RequestStatusOverdueRepository;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.database.model.RequestStatusOverdue;
import ru.sberbank.transport.oto.cargo.mappers.RequestStatusOverdueMapperImpl;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestStatusOverdueMessage;
import ru.sberbank.transport.oto.cargo.messaging.messages.TripRatingMessage;
import ru.sberbank.transport.oto.cargo.service.RequestService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@DisplayName("Проверка слушателя сообщений")
class ListenerConfigTest {

    private final ListenerConfig listenerConfig = new ListenerConfig();

    @Test
    @DisplayName("Проверка статуса превышения заявки")
    void test_requestStatusOverdueInput() {
        final var mapper = new RequestStatusOverdueMapperImpl();
        final var repository = mock(RequestStatusOverdueRepository.class);

        final var input = listenerConfig.requestStatusOverdueInput(mapper, repository);

        final var message = Instancio.of(RequestStatusOverdueMessage.class)
                .set(Select.field(RequestStatusOverdueMessage::tripRequestStatus), Instancio.create(TripRequestStatus.class).name())
                .set(Select.field(RequestStatusOverdueMessage::carsharingJoinRequestStatus), Instancio.create(CarsharingJoinRequestStatus.class).name())
                .create();

        input.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        final var messageCaptor = ArgumentCaptor.forClass(RequestStatusOverdue.class);

        verify(repository).save(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual).isNotNull();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(message.getId());
            it.assertThat(actual.getOverdueTime()).isEqualTo(message.overdueTime());
            it.assertThat(actual.getDeadlineValue()).isEqualTo(message.deadlineValue());
            it.assertThat(actual.getRequestId()).isEqualTo(message.requestId());
            it.assertThat(actual.getTripRequestStatus().name()).isEqualTo(message.tripRequestStatus());
            it.assertThat(actual.getDeadlineValue()).isEqualTo(message.deadlineValue());
        });
    }

    @Test
    @DisplayName("Проверка сообщения с рейтингом заявки")
    void test_requestRatingInput() {
        final var requestService = mock(RequestService.class);

        final var input = listenerConfig.requestRatingInput(requestService);
        final var message = Instancio.create(TripRatingMessage.class);
        final var request = Instancio.create(Request.class);

        when(requestService.findById(message.requestId())).thenReturn(Optional.of(request));

        input.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        final var requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestService).save(requestCaptor.capture());

        final var actual = requestCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getRequestRating().getAdvantages()).isEqualTo(message.advantages());
            it.assertThat(actual.getRequestRating().getDrawbacks()).isEqualTo(message.drawbacks());
            it.assertThat(actual.getRequestRating().getRating()).isEqualTo(message.rating());
            it.assertThat(actual.getRequestRating().getRatingComment()).isEqualTo(message.ratingComment());
        });
    }

    @Test
    @DisplayName("Проверка обновления срока выполнения заявки")
    void test_deadlineStateUpdateInput() {
        final var requestService = mock(RequestService.class);

        final var input = listenerConfig.deadlineStateUpdateInput(requestService);
        final var requestId = UUID.randomUUID();
        final var request = Request.builder().id(requestId).build();
        final var message = DeadlineStateUpdateMessage.builder()
                .requestId(requestId)
                .deadlineState(DeadlineState.RED.name())
                .deadlineTill(LocalDateTime.now().plusDays(1))
                .build();

        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(requestService.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        input.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, requestId))));

        final var requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestService).save(requestCaptor.capture());

        final var actual = requestCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(actual.getDeadlineState()).isEqualTo(DeadlineState.RED);
            it.assertThat(actual.getDeadline()).isEqualTo(message.getDeadlineTill());
        });
    }

    @Test
    @DisplayName("Проверка обновления срока выполнения заявки без id в message")
    void test_deadlineStateUpdateInput_noIdInMessage() {
        final var requestService = mock(RequestService.class);

        final var input = listenerConfig.deadlineStateUpdateInput(requestService);
        final var requestId = UUID.randomUUID();
        final var request = Request.builder().id(requestId).build();
        final var message = DeadlineStateUpdateMessage.builder()
                .requestId(requestId)
                .deadlineState(DeadlineState.RED.name())
                .deadlineTill(LocalDateTime.now().plusDays(1))
                .build();

        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(requestService.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        input.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, requestId))));

        final var requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(requestService).save(requestCaptor.capture());

        final var actual = requestCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(actual.getDeadlineState()).isEqualTo(DeadlineState.RED);
            it.assertThat(actual.getDeadline()).isEqualTo(message.getDeadlineTill());
        });
    }

}