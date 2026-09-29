package ru.sberbank.ditsib.transport.request.service.grpc.impl;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.request_checks.grpc.CheckMultipointLimitRequest;
import ru.sber.transport.request_checks.grpc.MultipointRequestCheckServiceGrpc;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestChecksGrpcServiceImpl implements RequestChecksGrpcService {

    @GrpcClient("request-checks")
    private MultipointRequestCheckServiceGrpc.MultipointRequestCheckServiceBlockingStub requestChecksClient;

    @Override
    public boolean isMultipointLimitExceeded(@NonNull UUID passengerId, @NonNull LocalDateTime desiredDate,
        @NonNull String timeZone) {
        try {
            requestChecksClient.checkMultipointLimit(CheckMultipointLimitRequest.newBuilder()
                .setPassengerId(passengerId.toString())
                .setDesiredDate(convertUtcDateTimeToTimestamp(desiredDate))
                .setTimeZone(timeZone)
                .build());
        } catch (StatusRuntimeException e) {
            var status = e.getStatus();
            if (status.getCode() == Status.Code.FAILED_PRECONDITION) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private Timestamp convertUtcDateTimeToTimestamp(LocalDateTime dateTime) {
        var instant = dateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

}
