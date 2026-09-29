package ru.sber.transport.request_checks.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;
import ru.sber.transport.request_checks.exception.DurationLimitExceededException;
import ru.sber.transport.request_checks.service.TripRequestService;

/**
 * gRPC сервис для проверки длительности поездок
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DurationRequestCheckGrpc extends
    RequestCheckServiceGrpc.RequestCheckServiceImplBase {

    private final TripRequestService tripRequestService;

    @Override
    public void checkDurationLimit(CheckDurationLimitRequest request,
        StreamObserver<CheckDurationLimitResponse> responseObserver) {
        try {
            val desiredOffset = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(request.getDesiredDate().getSeconds(), request.getDesiredDate().getNanos()),
                java.time.ZoneOffset.of(request.getTimeZone())
            );

            val dto = new DurationCheckRequestDto(
                UUID.fromString(request.getPassengerId()),
                desiredOffset,
                request.getExpectedDuration(),
                request.getTimeZone()
            );

            tripRequestService.checkDurationLimit(dto);

            CheckDurationLimitResponse response = CheckDurationLimitResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument in gRPC call: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException());
        } catch (DurationLimitExceededException e) {
            log.info("Duration limit exceeded: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in checkDurationLimit", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
        }
    }

}
