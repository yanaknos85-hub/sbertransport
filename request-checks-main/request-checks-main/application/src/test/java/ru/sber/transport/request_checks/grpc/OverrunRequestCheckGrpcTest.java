package ru.sber.transport.request_checks.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.protobuf.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.service.TripRequestService;

@DisplayName("Проверка gRPC сервиса OverrunRequestCheckGrpc")
class OverrunRequestCheckGrpcTest {

    private TripRequestService tripRequestService;

    private OverrunRequestCheckGrpc grpc;

    @BeforeEach
    void setup() {
        tripRequestService = mock(TripRequestService.class);
        grpc = new OverrunRequestCheckGrpc(tripRequestService);
    }

    private CheckOverrunLimitRequest createRequest(
        UUID passengerId, OffsetDateTime desiredDate, int expectedDistance) {

        ZonedDateTime zoned = desiredDate.atZoneSameInstant(ZoneId.of("+03:00"));
        val instant = zoned.toInstant();
        val timestamp = Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();

        return CheckOverrunLimitRequest.newBuilder()
            .setPassengerId(passengerId.toString())
            .setDesiredDate(timestamp)
            .setExpectedDistance(expectedDistance)
            .setTimeZone("+03:00")
            .build();
    }

    @Test
    @DisplayName("Успешная проверка - лимит не превыщен")
    void testCheckOverrunLimit_Success() {
        val passengerId = UUID.randomUUID();
        val date = OffsetDateTime.of(2026, 5, 21, 10, 30, 0, 0, ZoneOffset.of("+03:00"));
        val request = createRequest(passengerId, date, 100000);

        val dto = new OverrunCheckRequestDto(
            passengerId,
            date,
            100000,
            "+03:00"
        );
        
        val responseDto = new OverrunCheckResponseDto(null, 150000);
        doReturn(responseDto).when(tripRequestService).checkOverrunLimit(dto);

        val responseObserver = new TestStreamObserver<CheckOverrunLimitResponse>();

        grpc.checkOverrunLimit(request, responseObserver);

        assertThat(responseObserver.getResponse()).isNotNull();
        assertThat(responseObserver.getResponse().getComment()).isEmpty();
        assertThat(responseObserver.getResponse().getTotalDistance()).isEqualTo(150000);
        assertThat(responseObserver.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Проверка - лимит превышен (comment не пустой)")
    void testCheckOverrunLimit_LimitExceeded() {
        val passengerId = UUID.randomUUID();
        val date = OffsetDateTime.of(2026, 5, 21, 10, 30, 0, 0, ZoneOffset.of("+03:00"));
        val request = createRequest(passengerId, date, 100000);

        val dto = new OverrunCheckRequestDto(
            passengerId,
            date,
            100000,
            "+03:00"
        );
        
        val responseDto = new OverrunCheckResponseDto("Превышена суммарная протяженность поездок за месяц", 250000);
        doReturn(responseDto).when(tripRequestService).checkOverrunLimit(dto);

        val responseObserver = new TestStreamObserver<CheckOverrunLimitResponse>();

        grpc.checkOverrunLimit(request, responseObserver);

        assertThat(responseObserver.getResponse()).isNotNull();
        assertThat(responseObserver.getResponse().getComment()).isEqualTo("Превышена суммарная протяженность поездок за месяц");
        assertThat(responseObserver.getResponse().getTotalDistance()).isEqualTo(250000);
        assertThat(responseObserver.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Некорректный формат часового пояса")
    void testCheckOverrunLimit_InvalidTimeZone() {
        val passengerId = UUID.randomUUID();
        val date = OffsetDateTime.of(2026, 5, 21, 10, 30, 0, 0, ZoneOffset.of("+03:00"));
        val timestamp = Timestamp.newBuilder()
            .setSeconds(date.toInstant().getEpochSecond())
            .setNanos(date.toInstant().getNano())
            .build();

        val request = CheckOverrunLimitRequest.newBuilder()
            .setPassengerId(passengerId.toString())
            .setDesiredDate(timestamp)
            .setExpectedDistance(100000)
            .setTimeZone("Invalid/Timezone")
            .build();

        val responseObserver = new TestStreamObserver<CheckOverrunLimitResponse>();

        grpc.checkOverrunLimit(request, responseObserver);

        assertThat(responseObserver.getError()).isInstanceOf(io.grpc.StatusRuntimeException.class);
        val statusRuntimeException = (io.grpc.StatusRuntimeException) responseObserver.getError();
        assertThat(statusRuntimeException.getStatus().getCode())
            .isEqualTo(io.grpc.Status.INVALID_ARGUMENT.getCode());
    }

    @Test
    @DisplayName("Null значение ID пассажира")
    void testCheckOverrunLimit_NullPassengerId() {
        val timestamp = Timestamp.getDefaultInstance();

        val request = CheckOverrunLimitRequest.newBuilder()
            .setPassengerId("")
            .setDesiredDate(timestamp)
            .setExpectedDistance(100000)
            .setTimeZone("+03:00")
            .build();

        val responseObserver = new TestStreamObserver<CheckOverrunLimitResponse>();

        grpc.checkOverrunLimit(request, responseObserver);

        assertThat(responseObserver.getError()).isInstanceOf(io.grpc.StatusRuntimeException.class);
        val statusRuntimeException = (io.grpc.StatusRuntimeException) responseObserver.getError();
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
