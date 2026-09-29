package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.logging.model.LogField;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка отправки заявок в Kafka")
class RequestSenderTest extends KafkaTest {
    @Autowired
    private RequestSender<RequestForTaxi> requestForTaxiRequestSender;
    @Autowired
    private RequestSender<RequestForPersonal> requestForPersonalRequestSender;
    @Autowired
    private RequestSender<RequestForCarsharing> requestForCarsharingRequestSender;
    @Autowired
    private RequestSender<RequestForPublic> requestForPublicRequestSender;
    @MockitoSpyBean
    @Qualifier("requestOutput")
    private OutputBridge requestOutput;
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @Test
    @DisplayName("Отправка")
    void send() {
        MDC.clear();
        MDC.put(LogField.TRACE_ID.getKey(), UUID.randomUUID().toString());
        var requestForTaxi = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getTransportType), TransportTypeEnum.TAXI)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_SEARCH)
                .create();
        var requestForPersonal = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getTransportType), TransportTypeEnum.PERSONAL)
                .set(field(RequestForPersonal::getStatus), TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL)
                .create();
        var requestForCarsharing = Instancio.of(RequestForCarsharing.class)
                .set(field(RequestForCarsharing::getTransportType), TransportTypeEnum.CARSHARING)
                .set(field(RequestForCarsharing::getStatus), TripRequestStatus.CARSHARING_AWAITING_SEARCH)
                .create();
        var requestForPublic = Instancio.of(RequestForPublic.class)
                .set(field(RequestForPublic::getTransportType), TransportTypeEnum.PUBLIC)
                .set(field(RequestForPublic::getStatus), TripRequestStatus.PUBLIC_TRIP_CONFIRMATION)
                .create();
        requestForTaxiRequestSender.send(requestForTaxi, false);
        requestForPersonalRequestSender.send(requestForPersonal, false);
        requestForCarsharingRequestSender.send(requestForCarsharing);
        requestForPublicRequestSender.send(requestForPublic);
        var actual = consumeMessages("service.request", RequestMessage.class);
        verify(requestOutput, times(4)).send(any(RequestMessage.class), mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getAllValues())
                .hasSize(4)
                .extracting(
                        Map::keySet
                )
                .containsExactlyInAnyOrder(
                        Set.of("transportType", "messageId", "fraudHandled", LogField.TRACE_ID.getKey()),
                        Set.of("transportType", "messageId", "fraudHandled", LogField.TRACE_ID.getKey()),
                        Set.of("transportType", "messageId", "fraudHandled", LogField.TRACE_ID.getKey()),
                        Set.of("transportType", "messageId", "fraudHandled", LogField.TRACE_ID.getKey())
                );

        assertThat(actual)
                .hasSize(4)
                .extracting(
                        RequestMessage::getId,
                        RequestMessage::getHumanReadableId,
                        RequestMessage::getAuthorId,
                        RequestMessage::getPassengerId,
                        RequestMessage::getOrganizationId,
                        RequestMessage::getResolution,
                        RequestMessage::getCreationTime,
                        RequestMessage::getTimeZone,
                        RequestMessage::getFinishedTime,
                        RequestMessage::getTransportType,
                        RequestMessage::getTripClass,
                        RequestMessage::getApprovalId,
                        RequestMessage::getTariffId,
                        RequestMessage::getOutcomeTariffId,
                        RequestMessage::getApprovalDate,
                        RequestMessage::isCoopTrip,
                        RequestMessage::getTaxiTripHrId,
                        RequestMessage::getDesiredDate,
                        RequestMessage::getStatus,
                        RequestMessage::getMetricsStatus,
                        RequestMessage::getStatusCode,
                        RequestMessage::getPassengerCount,
                        RequestMessage::getApprovalState,
                        RequestMessage::getPurposeId,
                        RequestMessage::getCommentForDriver,
                        RequestMessage::isSharedRideOwner,
                        RequestMessage::getRideId,
                        RequestMessage::isDeleted
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                requestForTaxi.getId(),
                                requestForTaxi.getHumanReadableId(),
                                requestForTaxi.getAuthor().getId(),
                                requestForTaxi.getPassenger().getId(),
                                requestForTaxi.getOrganizationId(),
                                requestForTaxi.getResolution(),
                                requestForTaxi.getCreationTime(),
                                requestForTaxi.getTimeZone(),
                                requestForTaxi.getFinishedTime(),
                                requestForTaxi.getTransportType().name(),
                                requestForTaxi.getTaxiClass().name(),
                                requestForTaxi.getApprovedBy().getId(),
                                requestForTaxi.getTariffId(),
                                requestForTaxi.getOutcomeTariffId(),
                                requestForTaxi.getApprovalDate(),
                                requestForTaxi.isCoopTrip(),
                                requestForTaxi.getTaxiTrip().getHumanReadableId(),
                                requestForTaxi.getDesiredDate(),
                                requestForTaxi.getStatus().name(),
                                "IN_PROGRESS",
                                requestForTaxi.getStatusCode(),
                                requestForTaxi.getPassengerCount(),
                                requestForTaxi.getApprovalState().name(),
                                requestForTaxi.getPurpose().getId(),
                                requestForTaxi.getCommentForDriver(),
                                requestForTaxi.isSharedRideOwner(),
                                requestForTaxi.getRideId(),
                                false
                        ),
                        tuple(
                                requestForPersonal.getId(),
                                requestForPersonal.getHumanReadableId(),
                                requestForPersonal.getAuthor().getId(),
                                requestForPersonal.getPassenger().getId(),
                                requestForPersonal.getOrganizationId(),
                                null,
                                requestForPersonal.getCreationTime(),
                                requestForPersonal.getTimeZone(),
                                requestForPersonal.getFinishedTime(),
                                requestForPersonal.getTransportType().name(),
                                null,
                                requestForPersonal.getApprovedBy().getId(),
                                requestForPersonal.getTariffId(),
                                requestForPersonal.getOutcomeTariffId(),
                                requestForPersonal.getApprovalDate(),
                                requestForPersonal.isCoopTrip(),
                                null,
                                requestForPersonal.getDesiredDate(),
                                requestForPersonal.getStatus().name(),
                                "IN_PROGRESS",
                                requestForPersonal.getStatusCode(),
                                requestForPersonal.getPassengerCount(),
                                requestForPersonal.getApprovalState().name(),
                                requestForPersonal.getPurpose().getId(),
                                requestForPersonal.getCommentForDriver(),
                                requestForPersonal.isSharedRideOwner(),
                                requestForPersonal.getRideId(),
                                false
                        ),
                        tuple(
                                requestForCarsharing.getId(),
                                requestForCarsharing.getHumanReadableId(),
                                requestForCarsharing.getAuthor().getId(),
                                requestForCarsharing.getPassenger().getId(),
                                requestForCarsharing.getOrganizationId(),
                                null,
                                requestForCarsharing.getCreationTime(),
                                requestForCarsharing.getTimeZone(),
                                requestForCarsharing.getFinishedTime(),
                                requestForCarsharing.getTransportType().name(),
                                null,
                                requestForCarsharing.getApprovedBy().getId(),
                                requestForCarsharing.getTariffId(),
                                requestForCarsharing.getOutcomeTariffId(),
                                requestForCarsharing.getApprovalDate(),
                                requestForCarsharing.isCoopTrip(),
                                null,
                                requestForCarsharing.getDesiredDate(),
                                requestForCarsharing.getStatus().name(),
                                "IN_PROGRESS",
                                requestForCarsharing.getStatusCode(),
                                requestForCarsharing.getPassengerCount(),
                                requestForCarsharing.getApprovalState().name(),
                                requestForCarsharing.getPurpose().getId(),
                                null,
                                requestForCarsharing.isSharedRideOwner(),
                                requestForCarsharing.getRideId(),
                                false
                        ),
                        tuple(
                                requestForPublic.getId(),
                                requestForPublic.getHumanReadableId(),
                                requestForPublic.getAuthor().getId(),
                                requestForPublic.getPassenger().getId(),
                                requestForPublic.getOrganizationId(),
                                null,
                                requestForPublic.getCreationTime(),
                                requestForPublic.getTimeZone(),
                                null,
                                requestForPublic.getTransportType().name(),
                                null,
                                requestForPublic.getApprovedBy().getId(),
                                requestForPublic.getTariffId(),
                                requestForPublic.getOutcomeTariffId(),
                                requestForPublic.getApprovalDate(),
                                false,
                                null,
                                requestForPublic.getDesiredDate(),
                                requestForPublic.getStatus().name(),
                                "IN_PROGRESS",
                                requestForPublic.getStatusCode(),
                                0,
                                requestForPublic.getApprovalState().name(),
                                requestForPublic.getPurpose().getId(),
                                null,
                                false,
                                null,
                                false
                        )
                );
    }
}