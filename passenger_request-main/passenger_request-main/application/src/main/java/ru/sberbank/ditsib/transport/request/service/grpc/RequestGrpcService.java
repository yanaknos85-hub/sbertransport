package ru.sberbank.ditsib.transport.request.service.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.payout.grpc.dto.RequestDTO;
import ru.sber.transport.payout.grpc.service.RequestServiceGrpc;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RequestGrpcService extends RequestServiceGrpc.RequestServiceImplBase {

    private final RequestService requestService;

    @Override
    public void requestChangeStatus(RequestDTO.ChangeStatus request, StreamObserver<Empty> responseObserver) {
        try {
            var status = TripRequestStatus.valueOf(request.getNewStatus());
            var awaitingPaymentDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(
                            request.getAwaitingPaymentDateTime().getSeconds(),
                            request.getAwaitingPaymentDateTime().getNanos()
                    ),
                    ZoneOffset.UTC
            );
            var paidOutDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(
                            request.getPaidOutDateTime().getSeconds(),
                            request.getPaidOutDateTime().getNanos()
                    ),
                    ZoneOffset.UTC
            );
            for (var requestId : request.getRequestIdsList()) {
                var id = UUID.fromString(requestId);
                var optionalRequest = requestService.get(id);
                if (optionalRequest.isEmpty()) {
                    log.warn("Request with id {} not found", id);
                    continue;
                }
                if (status.equals(TripRequestStatus.PERSONAL_PAYMENT_AWAITING) || status.equals(TripRequestStatus.PERSONAL_PAYMENT_DONE)) {
                    var requestInDb = (RequestForPersonal) optionalRequest.get();
                    if (status.equals(TripRequestStatus.PERSONAL_PAYMENT_AWAITING)) {
                        requestInDb.setOrderPaymentFormationFinishingDate(awaitingPaymentDateTime);
                    }
                    if (status.equals(TripRequestStatus.PERSONAL_PAYMENT_DONE)) {
                        requestInDb.setFinishedTime(paidOutDateTime);
                    }
                    requestService.changeState(requestInDb, status);
                } else if (status.equals(TripRequestStatus.PUBLIC_PAYMENT_AWAITING) || status.equals(TripRequestStatus.PUBLIC_PAYMENT_DONE)) {
                    var requestInDb = (RequestForPublic) optionalRequest.get();
                    if (status.equals(TripRequestStatus.PUBLIC_PAYMENT_AWAITING)) {
                        requestInDb.setOrderPaymentFormationFinishingDate(awaitingPaymentDateTime);
                    }
                    requestService.changeState(requestInDb, status);
                } else {
                    throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
                }

            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
