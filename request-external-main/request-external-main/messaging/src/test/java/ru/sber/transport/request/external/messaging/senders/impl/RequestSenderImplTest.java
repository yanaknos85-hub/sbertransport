package ru.sber.transport.request.external.messaging.senders.impl;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import java.time.OffsetDateTime;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messages.request.external.avro.RequestMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.TestOrder;
import ru.sber.transport.request.external.messaging.TestWaypointData;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.senders.RequestSender;
import ru.sber.transport.request.external.model.TripOrderData;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка отправки данных в кафку")
class RequestSenderImplTest {

    private final OutputBridge kafkaBridge = mock(OutputBridge.class);
    private final OutputBridge kafkaSslBridge = mock(OutputBridge.class);
    private final OutputBridge avroBridge = mock(OutputBridge.class);
    private final RequestMapper requestMapper = mock(RequestMapper.class);

    private final RequestSender requestSender = new RequestSenderImpl(new SimpleObjectProvider<>(kafkaBridge),
            new SimpleObjectProvider<>(kafkaSslBridge),
            new SimpleObjectProvider<>(avroBridge), requestMapper);

    @Test
    @DisplayName("Отправка данных")
    void test_send() {
        final var fixedDate = OffsetDateTime.parse("2024-01-01T12:00:00Z");
        final var sourceWaypoints = Instancio.of(TestWaypointData.class).create();
        final var source = Instancio.of(TestOrder.class)
                .set(field(TestOrder::getDate), fixedDate)
                .set(field(TestOrder::getWaypoints), List.of(sourceWaypoints))
                .create();
        final var message = Instancio.create(ru.sber.transport.request.external.messaging.message.RequestMessage.class);
        final var avroMessage = Instancio.create(RequestMessage.class);

        doReturn(message).doReturn(message).when(requestMapper).toRequestMessage(any(TripOrderData.class), anyList());
        doReturn(avroMessage).when(requestMapper).toAvroRequestMessage(source);

        requestSender.send(source);

        final var sourceCaptor = ArgumentCaptor.forClass(RequestMessage.class);

        verify(kafkaBridge).send(any(ru.sber.transport.request.external.messaging.message.RequestMessage.class));
        verify(kafkaSslBridge).send(any(ru.sber.transport.request.external.messaging.message.RequestMessage.class));
        verify(avroBridge).send(sourceCaptor.capture());

        final var actual = sourceCaptor.getValue();
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(avroMessage.getId());
            it.assertThat(actual.getComment()).isEqualTo(avroMessage.getComment());
            it.assertThat(actual.getReason()).isEqualTo(avroMessage.getReason());
            it.assertThat(actual.getStatus().name()).isEqualTo(avroMessage.getStatus().name());
            it.assertThat(actual.getPlanned().getCost()).isEqualTo(avroMessage.getPlanned().getCost());
            it.assertThat(actual.getActual().getCost()).isEqualTo(avroMessage.getActual().getCost().doubleValue());
            it.assertThat(actual.getPlanned().getDuration()).isEqualTo(avroMessage.getPlanned().getDuration());
            it.assertThat(actual.getPlanned().getDistance()).isEqualTo(avroMessage.getPlanned().getDistance());
            it.assertThat(actual.getPassengerId()).isEqualTo(avroMessage.getPassengerId());
            it.assertThat(actual.getTariff().name()).isEqualTo(avroMessage.getTariff().name());
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(avroMessage.getWaypoints());
            it.assertThat(actual.getPurposeId()).isEqualTo(avroMessage.getPurposeId());
            it.assertThat(actual.getDate()).isEqualTo(avroMessage.getDate());
            it.assertThat(actual.getTimeZone()).isEqualTo(avroMessage.getTimeZone());
            it.assertThat(actual.getOrganizationId()).isEqualTo(avroMessage.getOrganizationId());
            it.assertThat(actual.getDepartmentId()).isEqualTo(avroMessage.getDepartmentId());
            it.assertThat(actual.getApprovalDate()).isEqualTo(avroMessage.getApprovalDate());
            it.assertThat(actual.getApproverId()).isEqualTo(avroMessage.getApproverId());
        });
    }

}
