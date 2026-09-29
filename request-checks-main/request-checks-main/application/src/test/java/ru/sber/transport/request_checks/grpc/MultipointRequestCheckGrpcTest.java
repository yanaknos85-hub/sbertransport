package ru.sber.transport.request_checks.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.service.TripRequestService;

@DisplayName("Проверка gRPC сервиса MultipointRequestCheckGrpc")
class MultipointRequestCheckGrpcTest {

    private TripRequestService tripRequestService;

    private MultipointRequestCheckGrpc grpc;

    @BeforeEach
    void setup() {
        tripRequestService = mock(TripRequestService.class);
        grpc = new MultipointRequestCheckGrpc(tripRequestService);
    }

    private CheckMultipointLimitRequest createRequest(
        UUID passengerId, LocalDateTime desiredDate) {

        val instant = desiredDate.toInstant(ZoneOffset.of("+03:00"));
        val timestamp = Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();

        return CheckMultipointLimitRequest.newBuilder()
            .setPassengerId(passengerId.toString())
            .setDesiredDate(timestamp)
            .setTimeZone("+03:00")
            .build();
    }

    @Test
    @DisplayName("Успешная проверка - лимит не превыщен")
    void testCheckMultipointLimit_Success() {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);
        val request = createRequest(passengerId, date);

        doNothing().when(tripRequestService).checkMultipointLimit(any(MultipointCheckRequestDto.class));

        val responseObserver = new TestStreamObserver<CheckMultipointLimitResponse>();

        grpc.checkMultipointLimit(request, responseObserver);

        assertThat(responseObserver.getResponse()).isNotNull();
        assertThat(responseObserver.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Проверка - лимит превышен (409)")
    void testCheckMultipointLimit_LimitExceeded() {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);
        val request = createRequest(passengerId, date);

        val exception = new MultipointRequestsLimitExceededException(
            "Превышен лимит поездок с количеством точек > 2");
        doThrow(exception).when(tripRequestService).checkMultipointLimit(any(MultipointCheckRequestDto.class));

        val responseObserver = new TestStreamObserver<CheckMultipointLimitResponse>();

        grpc.checkMultipointLimit(request, responseObserver);

        assertThat(responseObserver.getError()).isInstanceOf(StatusRuntimeException.class);
        val statusRuntimeException = (StatusRuntimeException) responseObserver.getError();
        assertThat(statusRuntimeException.getStatus().getCode())
            .isEqualTo(io.grpc.Status.FAILED_PRECONDITION.getCode());
        assertThat(statusRuntimeException.getStatus().getDescription())
            .contains("Превышен лимит поездок с количеством точек > 2");
    }

    @Test
    @DisplayName("Некорректный формат часового пояса")
    void testCheckMultipointLimit_InvalidTimeZone() {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);
        val timestamp = Timestamp.newBuilder()
            .setSeconds(date.toEpochSecond(ZoneOffset.UTC))
            .setNanos(0)
            .build();

        val request = CheckMultipointLimitRequest.newBuilder()
            .setPassengerId(passengerId.toString())
            .setDesiredDate(timestamp)
            .setTimeZone("Invalid/Timezone")
            .build();

        val responseObserver = new TestStreamObserver<CheckMultipointLimitResponse>();

        grpc.checkMultipointLimit(request, responseObserver);

        assertThat(responseObserver.getError()).isInstanceOf(StatusRuntimeException.class);
        val statusRuntimeException = (StatusRuntimeException) responseObserver.getError();
        assertThat(statusRuntimeException.getStatus().getCode())
            .isEqualTo(io.grpc.Status.INVALID_ARGUMENT.getCode());
    }

    @Test
    @DisplayName("Null значение ID пассажира")
    void testCheckMultipointLimit_NullPassengerId() {
        val timestamp = Timestamp.getDefaultInstance();

        val request = CheckMultipointLimitRequest.newBuilder()
            .setPassengerId("")
            .setDesiredDate(timestamp)
            .setTimeZone("+03:00")
            .build();

        val responseObserver = new TestStreamObserver<CheckMultipointLimitResponse>();

        grpc.checkMultipointLimit(request, responseObserver);

        assertThat(responseObserver.getError()).isInstanceOf(StatusRuntimeException.class);
        val statusRuntimeException = (StatusRuntimeException) responseObserver.getError();
        assertThat(statusRuntimeException.getStatus().getCode())
            .isEqualTo(io.grpc.Status.INVALID_ARGUMENT.getCode());
    }

    @Getter
    private static class TestStreamObserver<T> implements io.grpc.stub.StreamObserver<T> {

        private Throwable error;

        private T response;

        private boolean completed = false;

        @Override
        public void onNext(T value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }

    }

}
