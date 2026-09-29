package ru.sberbank.ditsib.transport.request.service.grpc;

import ru.sberbank.ditsib.transport.request.exceptions.DurationLimitExceededException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.request_checks.grpc.CheckDurationLimitRequest;
import ru.sber.transport.request_checks.grpc.CheckDurationLimitResponse;
import ru.sber.transport.request_checks.grpc.RequestCheckServiceGrpc;
import ru.sberbank.ditsib.transport.request.service.grpc.impl.DurationRequestCheckGrpcClientImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка работы DurationRequestCheckGrpcClientImpl")
class DurationRequestCheckGrpcClientImplTest {

    @Mock
    private RequestCheckServiceGrpc.RequestCheckServiceBlockingStub requestCheckService;

    @InjectMocks
    private DurationRequestCheckGrpcClientImpl durationRequestCheckGrpcClient;

    @Test
    @DisplayName("При успешной проверке duration не должен выбросить исключение")
    void checkDurationLimit_Success_ShouldNotThrowException() {
        var passengerId = UUID.randomUUID();
        var desiredDate = OffsetDateTime.now(ZoneOffset.UTC);
        var expectedDuration = Duration.ofHours(5).toMillis();
        var timeZone = "UTC";

        doReturn(CheckDurationLimitResponse.getDefaultInstance()).when(requestCheckService)
            .checkDurationLimit(any(CheckDurationLimitRequest.class));

        durationRequestCheckGrpcClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);
    }

    @Test
    @DisplayName("При превышении лимита duration (12 часов) должен выбросить DurationLimitExceededException")
    void checkDurationLimit_ExceedsLimit_ShouldThrowException() {
        var passengerId = UUID.randomUUID();
        var desiredDate = OffsetDateTime.now(ZoneOffset.UTC);
        var expectedDuration = Duration.ofHours(15).toMillis();
        var timeZone = "UTC";

        var statusRuntimeException = new StatusRuntimeException(Status.FAILED_PRECONDITION);

        doThrow(statusRuntimeException).when(requestCheckService)
            .checkDurationLimit(any(CheckDurationLimitRequest.class));

        assertThrows(DurationLimitExceededException.class, () ->
            durationRequestCheckGrpcClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone));
    }

    @Test
    @DisplayName("При любой другой ошибке gRPC должен только залогировать и не выбрасывать исключение")
    void checkDurationLimit_OtherError_ShouldNotThrowException() {
        var passengerId = UUID.randomUUID();
        var desiredDate = OffsetDateTime.now(ZoneOffset.UTC);
        var expectedDuration = Duration.ofHours(3).toMillis();
        var timeZone = "UTC";

        var otherException = new StatusRuntimeException(Status.INVALID_ARGUMENT);

        doThrow(otherException).when(requestCheckService)
            .checkDurationLimit(any(CheckDurationLimitRequest.class));

        durationRequestCheckGrpcClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);
    }

    @Test
    @DisplayName("При INTERNAL ошибке gRPC должен только залогировать и не выбрасывать исключение")
    void checkDurationLimit_InternalError_ShouldNotThrowException() {
        var passengerId = UUID.randomUUID();
        var desiredDate = OffsetDateTime.now(ZoneOffset.UTC);
        var expectedDuration = Duration.ofHours(3).toMillis();
        var timeZone = "UTC";

        var internalException = new StatusRuntimeException(Status.INTERNAL);

        doThrow(internalException).when(requestCheckService)
            .checkDurationLimit(any(CheckDurationLimitRequest.class));

        durationRequestCheckGrpcClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);
    }
}
