package ru.sber.transport.deadline.grpc.client;

import ru.sber.transport.deadline.grpc.common.dto.DeadlineRequestDto;
import ru.sber.transport.deadline.grpc.common.dto.DeadlineResultDto;
import ru.sber.transport.deadline.grpc.common.dto.ProlongateRequestDto;

public interface DeadlineCalculationClient {
    DeadlineResultDto deadlineCalculation(DeadlineRequestDto requestDto);
    DeadlineResultDto deadlineProlongation(ProlongateRequestDto requestDto);
}
