package ru.sber.transport.request.external.providers.grpc;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.business.providers.DurationRequestCheckProvider;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;
import ru.sber.transport.request_checks.grpc.CheckDurationLimitRequest;
import ru.sber.transport.request_checks.grpc.RequestCheckServiceGrpc;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * gRPC клиент для проверки длительности поездок в request_checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DurationRequestCheckProviderImpl implements DurationRequestCheckProvider {

    @GrpcClient("request-checks")
    private RequestCheckServiceGrpc.RequestCheckServiceBlockingStub requestCheckService;

    @Override
    public void checkDurationLimit(UUID passengerId, OffsetDateTime desiredDate, Long expectedDuration, String timeZone) {
        log.debug("Checking duration limit for passenger: {}, desiredDate: {}, expectedDuration: {}, timeZone: {}",
                passengerId, desiredDate, expectedDuration, timeZone);

        var request = CheckDurationLimitRequest.newBuilder()
                .setPassengerId(passengerId.toString())
                .setDesiredDate(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(desiredDate.toInstant().getEpochSecond())
                        .setNanos(desiredDate.toInstant().getNano())
                        .build())
                .setExpectedDuration(expectedDuration)
                .setTimeZone(timeZone)
                .build();

        try {
            requestCheckService.checkDurationLimit(request);
            log.debug("Duration check passed for passenger: {}", passengerId);
        } catch (StatusRuntimeException e) {
            var statusCode = e.getStatus().getCode();
            var statusDescription = e.getStatus().getDescription();
            log.error("Duration check failed for passenger: {} with status: {}, description: {}",
                    passengerId, statusCode, statusDescription, e);
            if (statusCode == io.grpc.Status.Code.FAILED_PRECONDITION) {
                throw new DurationLimitExceededException();
            }
        } catch (Exception e) {
            log.error("Unknown error for check-duration");
        }
    }
}
