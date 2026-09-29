package ru.sber.transport.request.external.providers.grpc;

import io.grpc.StatusRuntimeException;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.business.providers.OverrunCheckProvider;
import ru.sber.transport.request.external.model.overrun.OverrunCheckResult;
import ru.sber.transport.request_checks.grpc.CheckOverrunLimitRequest;
import ru.sber.transport.request_checks.grpc.OverrunRequestCheckServiceGrpc;

/**
 * gRPC клиент для проверки суммарного километража в request_checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverrunRequestCheckProviderImpl implements OverrunCheckProvider {

    @GrpcClient("request-checks")
    private OverrunRequestCheckServiceGrpc.OverrunRequestCheckServiceBlockingStub overrunRequestCheckService;

    @Override
    public OverrunCheckResult checkOverrunLimit(UUID passengerId, OffsetDateTime desiredDate,
                                                int expectedDistance, String timeZone) {
        log.debug("Checking overrun limit for passenger: {}, expectedDistance: {}, timeZone: {}",
                passengerId, expectedDistance, timeZone);

        var request = CheckOverrunLimitRequest.newBuilder()
                .setPassengerId(passengerId.toString())
                .setDesiredDate(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(desiredDate.toInstant().getEpochSecond())
                        .setNanos(desiredDate.toInstant().getNano())
                        .build())
                .setExpectedDistance(expectedDistance)
                .setTimeZone(timeZone)
                .build();

        try {
            var response = overrunRequestCheckService.checkOverrunLimit(request);
            var comment = response.getComment();
            var totalDistance = response.getTotalDistance();
            log.debug("Overrun check result for passenger: {}: comment={}, totalDistance={}",
                    passengerId, comment, totalDistance);
            return new OverrunCheckResult(comment, totalDistance);
        } catch (StatusRuntimeException e) {
            var statusCode = e.getStatus().getCode();
            var statusDescription = e.getStatus().getDescription();
            log.error("Overrun check failed for passenger: {} with status: {}, description: {}",
                    passengerId, statusCode, statusDescription, e);
            return null;
        } catch (Exception e) {
            log.error("Unknown error for check-overrun");
            return null;
        }
    }
}
