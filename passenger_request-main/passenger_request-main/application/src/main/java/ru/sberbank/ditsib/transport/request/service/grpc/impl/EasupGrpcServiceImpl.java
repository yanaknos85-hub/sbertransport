package ru.sberbank.ditsib.transport.request.service.grpc.impl;

import com.google.protobuf.Int64Value;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import ru.sber.transport.easup.grpc.dto.Dto;
import ru.sber.transport.easup.grpc.service.EmployeeAbsenceServiceGrpc;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceResponse;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class EasupGrpcServiceImpl implements EasupGrpcService {

    @GrpcClient("easup")
    private EmployeeAbsenceServiceGrpc.EmployeeAbsenceServiceBlockingStub easupClient;

    @Override
    public Optional<EasupAbsenceResponse> resolveAbsence(EasupAbsenceRequest request) {
        try {
            var response = easupClient.getEmployeeAbsence(
                    Dto.EmployeeAbsenceRequest.newBuilder()
                            .setDesiredDate(request.desiredDate())
                            .setTimeZone(request.timeZone())
                            .setPersonnelNumber(request.personnelNumber())
                            .setExpectedDuration(Int64Value.of(request.expectedDuration()))
                            .build());
            return Optional.of(new EasupAbsenceResponse(response.getType(), response.getStartDate().getValue(),
                    response.getEndDate().getValue()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
