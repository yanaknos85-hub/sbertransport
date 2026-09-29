package ru.sberbank.ditsib.transport.request.service.grpc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.assertj.core.groups.Tuple;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.payout.grpc.dto.RequestDTO;
import ru.sberbank.ditsib.transport.LoggingExtension;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestGrpcServiceTest {

    @Mock
    private RequestService requestService;
    @Mock
    private StreamObserver<Empty> responseObserver;

    @InjectMocks
    private RequestGrpcService requestGrpcService;

    @Captor
    private ArgumentCaptor<RequestForPersonal> requestForPersonalArgumentCapture;
    @Captor
    private ArgumentCaptor<RequestForPublic> requestForPublicArgumentCaptor;

    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(RequestGrpcService.class);

    @Test
    void requestChangeStatus() {
        var requestId1 = UUID.randomUUID();
        var requestId2 = UUID.randomUUID();
        var request = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId1.toString(), requestId2.toString()))
                .setNewStatus(TripRequestStatus.PUBLIC_PAYMENT_AWAITING.name())
                .build();
        var request1 = Instancio.of(RequestForPublic.class)
                .set(field(RequestForPublic::getId), requestId1)
                .create();

        doReturn(Optional.of(request1)).when(requestService).get(requestId1);
        doReturn(Optional.empty()).when(requestService).get(requestId2);

        requestGrpcService.requestChangeStatus(request, responseObserver);

        verify(requestService).changeState(request1, TripRequestStatus.PUBLIC_PAYMENT_AWAITING);
        verify(responseObserver).onNext(Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();

        assertThat(LOGGING_EXTENSION.getEvents())
                .isNotEmpty()
                .hasSize(1)
                .extracting(
                        ILoggingEvent::getLoggerName,
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getFormattedMessage
                )
                .containsExactlyInAnyOrder(
                        Tuple.tuple(
                                RequestGrpcService.class.getName(),
                                Level.WARN,
                                "Request with id %s not found".formatted(requestId2)
                        )
                );
    }

    @Test
    void requestChangeStatus_personalStatuses() {
        var requestId = UUID.randomUUID();
        var instant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        var request1 = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name())
                .setAwaitingPaymentDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .setPaidOutDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .build();
        var requestFromDb1 = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getId), requestId)
                .set(field(RequestForPersonal::getFinishedTime), null)
                .create();

        doReturn(Optional.of(requestFromDb1)).when(requestService).get(requestId);

        requestGrpcService.requestChangeStatus(request1, responseObserver);

        verify(requestService).changeState(requestForPersonalArgumentCapture.capture(), eq(TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
        verify(responseObserver).onNext(Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();

        assertThat(requestForPersonalArgumentCapture.getValue())
                .extracting(
                        RequestForPersonal::getId,
                        RequestForPersonal::getOrderPaymentFormationFinishingDate,
                        RequestForPersonal::getFinishedTime
                )
                .containsExactlyInAnyOrder(
                        requestId,
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(
                                        instant.getEpochSecond(),
                                        instant.getNano()
                                ),
                                ZoneOffset.UTC
                        ),
                        null
                );

        var request2 = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.PERSONAL_PAYMENT_DONE.name())
                .setAwaitingPaymentDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .setPaidOutDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .build();
        var requestFromDb2 = Instancio.of(RequestForPersonal.class)
                .set(field(RequestForPersonal::getId), requestId)
                .set(field(RequestForPersonal::getOrderPaymentFormationFinishingDate), null)
                .create();

        doReturn(Optional.of(requestFromDb2)).when(requestService).get(requestId);

        requestGrpcService.requestChangeStatus(request2, responseObserver);

        verify(requestService).changeState(requestForPersonalArgumentCapture.capture(), eq(TripRequestStatus.PERSONAL_PAYMENT_DONE));
        verify(responseObserver, times(2)).onNext(Empty.getDefaultInstance());
        verify(responseObserver, times(2)).onCompleted();

        assertThat(requestForPersonalArgumentCapture.getValue())
                .extracting(
                        RequestForPersonal::getId,
                        RequestForPersonal::getOrderPaymentFormationFinishingDate,
                        RequestForPersonal::getFinishedTime
                )
                .containsExactlyInAnyOrder(
                        requestId,
                        null,
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(
                                        instant.getEpochSecond(),
                                        instant.getNano()
                                ),
                                ZoneOffset.UTC
                        )
                );

        var request3 = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.CARSHARING_TRIP_FINISHED.name())
                .build();

        requestGrpcService.requestChangeStatus(request3, responseObserver);

        verify(responseObserver).onError(any());
    }

    @Test
    void requestChangeStatus_publicStatuses() {
        var requestId = UUID.randomUUID();
        var instant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        var request1 = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.PUBLIC_PAYMENT_AWAITING.name())
                .setAwaitingPaymentDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .setPaidOutDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .build();
        var requestFromDb1 = Instancio.of(RequestForPublic.class)
                .set(field(RequestForPublic::getId), requestId)
                .create();

        doReturn(Optional.of(requestFromDb1)).when(requestService).get(requestId);

        requestGrpcService.requestChangeStatus(request1, responseObserver);

        verify(requestService).changeState(requestForPublicArgumentCaptor.capture(), eq(TripRequestStatus.PUBLIC_PAYMENT_AWAITING));
        verify(responseObserver).onNext(Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();

        assertThat(requestForPublicArgumentCaptor.getValue())
                .extracting(
                        RequestForPublic::getId,
                        RequestForPublic::getOrderPaymentFormationFinishingDate
                )
                .containsExactlyInAnyOrder(
                        requestId,
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(
                                        instant.getEpochSecond(),
                                        instant.getNano()
                                ),
                                ZoneOffset.UTC
                        )
                );

        var request2 = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.PUBLIC_PAYMENT_DONE.name())
                .setAwaitingPaymentDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .setPaidOutDateTime(
                        Timestamp.newBuilder()
                                .setSeconds(instant.getEpochSecond())
                                .setNanos(instant.getNano())
                                .build()
                )
                .build();
        var requestFromDb2 = Instancio.of(RequestForPublic.class)
                .set(field(RequestForPublic::getId), requestId)
                .set(field(RequestForPublic::getOrderPaymentFormationFinishingDate), null)
                .create();

        doReturn(Optional.of(requestFromDb2)).when(requestService).get(requestId);

        requestGrpcService.requestChangeStatus(request2, responseObserver);

        verify(requestService).changeState(requestForPublicArgumentCaptor.capture(), eq(TripRequestStatus.PUBLIC_PAYMENT_DONE));
        verify(responseObserver, times(2)).onNext(Empty.getDefaultInstance());
        verify(responseObserver, times(2)).onCompleted();

        assertThat(requestForPublicArgumentCaptor.getValue())
                .extracting(
                        RequestForPublic::getId,
                        RequestForPublic::getOrderPaymentFormationFinishingDate
                )
                .containsExactlyInAnyOrder(
                        requestId,
                        null
                );
    }

    @Test
    void requestChangeStatus_illegalStatus() {
        var requestId = UUID.randomUUID();
        var request = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId.toString()))
                .setNewStatus(TripRequestStatus.CARSHARING_TRIP_FINISHED.name())
                .build();

        doReturn(Optional.of(Instancio.create(RequestForCarsharing.class)))
                .when(requestService).get(requestId);

        requestGrpcService.requestChangeStatus(request, responseObserver);

        verify(responseObserver).onError(any());
    }

    @Test
    void requestChangeStatus_throws_exception() {
        var requestId1 = "wrong_uuid";
        var request = RequestDTO.ChangeStatus.newBuilder()
                .addAllRequestIds(List.of(requestId1))
                .setNewStatus(TripRequestStatus.PUBLIC_PAYMENT_AWAITING.name())
                .build();

        requestGrpcService.requestChangeStatus(request, responseObserver);

        verify(responseObserver).onError(any(Throwable.class));
    }
}