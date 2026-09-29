package ru.sber.transport.request_checks.grpc;

import static ru.sber.transport.request_checks.util.TimeUtils.toLocalDateTime;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.DateTimeException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.service.TripRequestService;

/**
 * gRPC сервис для проверки многоточечных поездок
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class MultipointRequestCheckGrpc extends
    MultipointRequestCheckServiceGrpc.MultipointRequestCheckServiceImplBase {

    private final TripRequestService tripRequestService;

    @Override
    public void checkMultipointLimit(CheckMultipointLimitRequest request,
        StreamObserver<CheckMultipointLimitResponse> responseObserver) {
        try {
            val desiredLocal = toLocalDateTime(request.getDesiredDate(), request.getTimeZone());

            val dto = new MultipointCheckRequestDto(
                UUID.fromString(request.getPassengerId()),
                desiredLocal,
                request.getTimeZone()
            );

            tripRequestService.checkMultipointLimit(dto);

            CheckMultipointLimitResponse response = CheckMultipointLimitResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException | DateTimeException e) {
            log.warn("Invalid argument in gRPC call: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException());
        } catch (MultipointRequestsLimitExceededException e) {
            log.info("Multipoint limit exceeded: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in checkMultipointLimit", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
        }
    }

}
