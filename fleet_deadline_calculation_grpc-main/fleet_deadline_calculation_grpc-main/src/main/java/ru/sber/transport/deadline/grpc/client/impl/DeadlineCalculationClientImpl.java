package ru.sber.transport.deadline.grpc.client.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.deadline.grpc.client.DeadlineCalculationClient;
import ru.sber.transport.deadline.grpc.common.dto.DeadlineRequestDto;
import ru.sber.transport.deadline.grpc.common.dto.DeadlineResultDto;
import ru.sber.transport.deadline.grpc.common.dto.ProlongateRequestDto;
import ru.sber.transport.deadline.grpc.common.helper.GrpcTypesHelper;
import ru.sber.transport.deadline.grpc.dto.DeadlineRequest;
import ru.sber.transport.deadline.grpc.dto.ProlongationRequest;
import ru.sber.transport.deadline.grpc.service.DeadlineGrpcServiceGrpc;

@Component
@RequiredArgsConstructor
class DeadlineCalculationClientImpl implements DeadlineCalculationClient {
    
    @GrpcClient("grpc-deadline-calculation")
    private DeadlineGrpcServiceGrpc.DeadlineGrpcServiceBlockingStub deadlineGrpcServiceBlockingStub;
    
    @Override
    public DeadlineResultDto deadlineCalculation(@Valid DeadlineRequestDto requestDto) {
        var dateTime = GrpcTypesHelper.fromLocalDateTime(requestDto.dateTime());
        var unit = GrpcTypesHelper.convertUnit(requestDto.unit());
        var request = DeadlineRequest.newBuilder()
                                     .setStartDateTime(dateTime)
                                     .setUnit(unit)
                                     .setCount(requestDto.count())
                                     .build();
        var response = deadlineGrpcServiceBlockingStub.calculateDeadline(request);
        return new DeadlineResultDto(GrpcTypesHelper.toLocalDateTime(response.getDeadlineDateTime()));
    }
    
    @Override
    public DeadlineResultDto deadlineProlongation(@Valid ProlongateRequestDto requestDto) {
        var dateTime = GrpcTypesHelper.fromLocalDateTime(requestDto.dateTime());
        var unit = GrpcTypesHelper.convertUnit(requestDto.unit());
        var request = ProlongationRequest.newBuilder()
                                         .setDateTime(dateTime)
                                         .setUnit(unit)
                                         .setCount(requestDto.count())
                                         .build();
        var response = deadlineGrpcServiceBlockingStub.prolongateDeadline(request);
        return new DeadlineResultDto(GrpcTypesHelper.toLocalDateTime(response.getDeadlineDateTime()));
    }
}
