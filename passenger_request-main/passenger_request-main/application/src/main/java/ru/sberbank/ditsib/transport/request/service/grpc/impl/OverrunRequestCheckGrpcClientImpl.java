package ru.sberbank.ditsib.transport.request.service.grpc.impl;

import io.grpc.StatusRuntimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitRequest;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitResponse;
import ru.sber.transport.request_checks.grpc.OverrunRequestCheckServiceGrpc;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;

/**
 * gRPC клиент для проверки превышения лимита суммарного километража.
 */
@Slf4j
@Service
public class OverrunRequestCheckGrpcClientImpl implements OverrunRequestCheckGrpcClient {

    @GrpcClient("request-checks")
    private OverrunRequestCheckServiceGrpc.OverrunRequestCheckServiceBlockingStub overrunCheckService;

    @Override
    public CheckOverrunLimitResponse checkOverrunLimit(UUID passengerId, LocalDateTime desiredDate,
        int expectedDistance, String timeZone) {
        log.debug("Checking overrun limit for passenger: {}, expectedDistance: {}, timeZone: {}",
            passengerId, expectedDistance, timeZone);

        var request = CheckOverrunLimitRequest.newBuilder()
            .setPassengerId(passengerId.toString())
            .setDesiredDate(com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(desiredDate.toInstant(ZoneOffset.UTC).getEpochSecond())
                .setNanos(desiredDate.toInstant(ZoneOffset.UTC).getNano())
                .build())
            .setExpectedDistance(expectedDistance)
            .setTimeZone(timeZone)
            .build();

        try {
            var response = overrunCheckService.checkOverrunLimit(request);
            log.debug("Overrun check response for passenger {}: comment='{}', totalDistance={}",
                passengerId, response.getComment(), response.getTotalDistance());
            return response;
        } catch (StatusRuntimeException e) {
            var statusCode = e.getStatus().getCode();
            var statusDescription = e.getStatus().getDescription();
            log.warn("Overrun check gRPC failed for passenger {} with status: {}, description: {}",
                passengerId, statusCode, statusDescription);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error in checkOverrunLimit for passenger {}", passengerId, e);
            return null;
        }
    }

}