package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.request_checks.grpc.CheckDurationLimitRequest;
import ru.sber.transport.request_checks.grpc.CheckDurationLimitResponse;
import ru.sber.transport.request_checks.grpc.RequestCheckServiceGrpc;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;
import ru.sber.transport.business.providers.DurationRequestCheckProvider;

@UnitTest
@IsolatedTest
@Isolated
@DisplayName("Проверка gRPC клиента для проверки длительности поездок")
class DurationRequestCheckProviderImplTest {

    private RequestCheckServiceGrpc.RequestCheckServiceBlockingStub stubMock;

    private DurationRequestCheckProvider durationClient;

    @BeforeEach
    void setUp() {
        stubMock = mock(RequestCheckServiceGrpc.RequestCheckServiceBlockingStub.class);
        durationClient = new DurationRequestCheckProviderImpl();
        
        // Используем reflection для установки стуба, так как он аннотирован @GrpcClient
        try {
            var field = DurationRequestCheckProviderImpl.class.getDeclaredField("requestCheckService");
            field.setAccessible(true);
            field.set(durationClient, stubMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Проверка успешной проверки длительности")
    void test_checkDurationLimit_success() {
        var passengerId = java.util.UUID.randomUUID();
        var desiredDate = java.time.OffsetDateTime.now();
        var expectedDuration = 3600000L; // 1 час в миллисекундах
        var timeZone = "+03:00";

        var expectedResponse = CheckDurationLimitResponse.getDefaultInstance();
        when(stubMock.checkDurationLimit(any(CheckDurationLimitRequest.class)))
                .thenReturn(expectedResponse);

        durationClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);

        verify(stubMock).checkDurationLimit(any(CheckDurationLimitRequest.class));
    }

    @Test
    @DisplayName("Проверка проверки длительности с превышением лимита (FAILED_PRECONDITION)")
    void test_checkDurationLimit_failure() {
        var passengerId = java.util.UUID.randomUUID();
        var desiredDate = java.time.OffsetDateTime.now();
        var expectedDuration = 3600000L;
        var timeZone = "+03:00";

        var grpcException = Status.FAILED_PRECONDITION
                .withDescription("Duration limit exceeded")
                .asRuntimeException();
        when(stubMock.checkDurationLimit(any(CheckDurationLimitRequest.class)))
                .thenThrow(grpcException);

        assertThatThrownBy(() -> durationClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone))
                .isInstanceOf(DurationLimitExceededException.class)
                .hasMessage("Превышен лимит длительности поездок");

        verify(stubMock).checkDurationLimit(any(CheckDurationLimitRequest.class));
    }

    @Test
    @DisplayName("Проверка проверки длительности с нулевой длительностью")
    void test_checkDurationLimit_zeroDuration() {
        var passengerId = java.util.UUID.randomUUID();
        var desiredDate = java.time.OffsetDateTime.now();
        var expectedDuration = 0L;
        var timeZone = "+03:00";

        var expectedResponse = CheckDurationLimitResponse.getDefaultInstance();
        when(stubMock.checkDurationLimit(any(CheckDurationLimitRequest.class)))
                .thenReturn(expectedResponse);

        durationClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);

        verify(stubMock).checkDurationLimit(any(CheckDurationLimitRequest.class));
    }

    @Test
    @DisplayName("Проверка проверки длительности с отрицательной длительностью")
    void test_checkDurationLimit_negativeDuration() {
        var passengerId = java.util.UUID.randomUUID();
        var desiredDate = java.time.OffsetDateTime.now();
        var expectedDuration = -1000L;
        var timeZone = "+03:00";

        var expectedResponse = CheckDurationLimitResponse.getDefaultInstance();
        when(stubMock.checkDurationLimit(any(CheckDurationLimitRequest.class)))
                .thenReturn(expectedResponse);

        durationClient.checkDurationLimit(passengerId, desiredDate, expectedDuration, timeZone);

        verify(stubMock).checkDurationLimit(any(CheckDurationLimitRequest.class));
    }
}
