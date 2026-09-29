package ru.sber.transport.deadline.grpc.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.deadline.grpc.common.dto.DeadlineRequestDto;
import ru.sber.transport.deadline.grpc.common.dto.DeadlineResultDto;
import ru.sber.transport.deadline.grpc.common.dto.ProlongateRequestDto;
import ru.sber.transport.deadline.grpc.common.dto.Unit;
import ru.sber.transport.deadline.grpc.common.helper.GrpcTypesHelper;
import ru.sber.transport.deadline.grpc.dto.DeadlineResponse;
import ru.sber.transport.deadline.grpc.service.DeadlineGrpcServiceGrpc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DeadlineCalculationClientImplTest {
    
    @InjectMocks
    private DeadlineCalculationClientImpl deadlineCalculationClient;
    @Mock
    private DeadlineGrpcServiceGrpc.DeadlineGrpcServiceBlockingStub grpcServiceBlockingStub;
    
    @Test
    void calculation() {
        Mockito.doReturn(DeadlineResponse.newBuilder()
                                 .setDeadlineDateTime(GrpcTypesHelper.fromLocalDate(
                                         LocalDate.of(2024, 5, 24)))
                                 .build())
                .when(grpcServiceBlockingStub).calculateDeadline(any());
        
        var deadlineResultDto =
                deadlineCalculationClient.deadlineCalculation(new DeadlineRequestDto(LocalDateTime.now(), Unit.BUSINESS_HOUR, 10));
        assertThat(deadlineResultDto)
                .isNotNull()
                .extracting(DeadlineResultDto::deadline)
                .isEqualTo(LocalDate.of(2024, 5, 24).atStartOfDay());
    }
    
    @Test
    void prolongation() {
        Mockito.doReturn(DeadlineResponse.newBuilder()
                                 .setDeadlineDateTime(GrpcTypesHelper.fromLocalDate(
                                         LocalDate.of(2024, 5, 24)))
                                 .build())
                .when(grpcServiceBlockingStub).prolongateDeadline(any());
        
        var deadlineResultDto =
                deadlineCalculationClient.deadlineProlongation(new ProlongateRequestDto(LocalDateTime.now(), Unit.BUSINESS_HOUR, 10));
        assertThat(deadlineResultDto)
                .isNotNull()
                .extracting(DeadlineResultDto::deadline)
                .isEqualTo(LocalDate.of(2024, 5, 24).atStartOfDay());
    }
    
}
