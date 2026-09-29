package ru.sberbank.ditsib.transport.request.service.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.request_checks.grpc.CheckMultipointLimitResponse;
import ru.sber.transport.request_checks.grpc.MultipointRequestCheckServiceGrpc;
import ru.sberbank.ditsib.transport.request.service.grpc.impl.RequestChecksGrpcServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка работы RequestChecksGrpcServiceImpl")
class RequestChecksGrpcServiceImplTest {

    @Mock
    private MultipointRequestCheckServiceGrpc.MultipointRequestCheckServiceBlockingStub requestChecksClient;

    @InjectMocks
    private RequestChecksGrpcServiceImpl requestChecksGrpcService;

    @Test
    @DisplayName("При превышении лимита многоточечных поездок должен вернуть true")
    void isMultipointLimit_ExceedsLimit_Exceeded_ReturnsTrue() {
        var passengerId = Instancio.create(UUID.class);
        var desiredDate = Instancio.create(LocalDateTime.class);
        var timeZone = "UTC";

        var statusRuntimeException = new StatusRuntimeException(Status.FAILED_PRECONDITION);

        doThrow(statusRuntimeException).when(requestChecksClient).checkMultipointLimit(any());

        var response = requestChecksGrpcService.isMultipointLimitExceeded(passengerId, desiredDate, timeZone);

        assertThat(response).isTrue();
    }

    @Test
    @DisplayName("При успешной проверке должен вернуть false")
    void isMultipointLimit_Exceeded_Success_ReturnsFalse() {
        var passengerId = Instancio.create(UUID.class);
        var desiredDate = Instancio.create(LocalDateTime.class);
        var timeZone = "UTC";

        doReturn(CheckMultipointLimitResponse.getDefaultInstance()).when(requestChecksClient)
            .checkMultipointLimit(any());

        var response = requestChecksGrpcService.isMultipointLimitExceeded(passengerId, desiredDate, timeZone);

        assertThat(response).isFalse();
    }

    @Test
    @DisplayName("При любой другой ошибке должен вернуть false")
    void isMultipointLimit_Exceeded_OtherError_ReturnsFalse() {
        var passengerId = Instancio.create(UUID.class);
        var desiredDate = Instancio.create(LocalDateTime.class);
        var timeZone = "UTC";

        doThrow(new RuntimeException("Some error")).when(requestChecksClient).checkMultipointLimit(any());

        var response = requestChecksGrpcService.isMultipointLimitExceeded(passengerId, desiredDate, timeZone);

        assertThat(response).isFalse();
    }

}
