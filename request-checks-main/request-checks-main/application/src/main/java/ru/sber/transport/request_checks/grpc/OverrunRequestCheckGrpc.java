package ru.sber.transport.request_checks.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.service.TripRequestService;

import java.time.DateTimeException;

/**
 * gRPC сервис для проверки суммарного километража
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class OverrunRequestCheckGrpc extends
    OverrunRequestCheckServiceGrpc.OverrunRequestCheckServiceImplBase {

    private final TripRequestService tripRequestService;

    @Override
    public void checkOverrunLimit(CheckOverrunLimitRequest request,
        StreamObserver<CheckOverrunLimitResponse> responseObserver) {
        try {
            val desiredOffset = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(request.getDesiredDate().getSeconds(), request.getDesiredDate().getNanos()),
                java.time.ZoneOffset.of(request.getTimeZone())
            );

            val dto = new OverrunCheckRequestDto(
                UUID.fromString(request.getPassengerId()),
                desiredOffset,
                request.getExpectedDistance(),
                request.getTimeZone()
            );

            OverrunCheckResponseDto responseDto = tripRequestService.checkOverrunLimit(dto);

            CheckOverrunLimitResponse response = CheckOverrunLimitResponse.newBuilder()
                .setComment(responseDto.comment() != null ? responseDto.comment() : "")
                .setTotalDistance(responseDto.totalDistance())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException | DateTimeException e) {
            log.warn("Invalid argument in gRPC call: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error in checkOverrunLimit", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException());
        }
    }

}
