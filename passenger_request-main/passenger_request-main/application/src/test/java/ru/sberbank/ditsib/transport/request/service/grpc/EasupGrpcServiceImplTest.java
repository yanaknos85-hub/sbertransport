package ru.sberbank.ditsib.transport.request.service.grpc;

import com.google.protobuf.Int64Value;
import io.grpc.StatusRuntimeException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.easup.grpc.dto.Dto;
import ru.sber.transport.easup.grpc.service.EmployeeAbsenceServiceGrpc;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.service.grpc.impl.EasupGrpcServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка работы сервиса взаимодействия с Easup")
class EasupGrpcServiceImplTest {

    @Mock
    private EmployeeAbsenceServiceGrpc.EmployeeAbsenceServiceBlockingStub easupClient;
    @InjectMocks
    private EasupGrpcServiceImpl easupGrpcService;

    @Test
    void resolveAbsence() {
        var request1 = Instancio.create(EasupAbsenceRequest.class);
        var request2 = Instancio.create(EasupAbsenceRequest.class);

        doThrow(StatusRuntimeException.class).when(easupClient).getEmployeeAbsence(
                Dto.EmployeeAbsenceRequest.newBuilder()
                        .setDesiredDate(request1.desiredDate())
                        .setTimeZone(request1.timeZone())
                        .setPersonnelNumber(request1.personnelNumber())
                        .setExpectedDuration(Int64Value.of(request1.expectedDuration()))
                        .build());

        doReturn(Dto.EmployeeAbsenceResponse.newBuilder()
                .setType("ABSENCE")
                .setStartDate(Int64Value.of(1L))
                .setEndDate(Int64Value.of(2L))
                .build())
                .when(easupClient).getEmployeeAbsence(
                        Dto.EmployeeAbsenceRequest.newBuilder()
                                .setDesiredDate(request2.desiredDate())
                                .setTimeZone(request2.timeZone())
                                .setPersonnelNumber(request2.personnelNumber())
                                .setExpectedDuration(Int64Value.of(request2.expectedDuration()))
                                .build());

        var response1 = easupGrpcService.resolveAbsence(request1);
        var response2 = easupGrpcService.resolveAbsence(request2);

        assertThat(response1).isEmpty();
        assertThat(response2).isNotEmpty();
    }
}