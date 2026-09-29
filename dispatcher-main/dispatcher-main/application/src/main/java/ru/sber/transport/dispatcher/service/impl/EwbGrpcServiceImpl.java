package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.dispatcher.service.EwbGrpcService;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EwbGrpcServiceImpl implements EwbGrpcService {

    @GrpcClient("ewb")
    private EwbServiceGrpc.EwbServiceBlockingStub stub;

    @Override
    public List<FirstTitleResponseDto> send(List<ShiftForEwbDto> shifts) {
        var requestsListBuilder = Dto.FirstTitleRequestList.newBuilder();
        shifts.forEach(shift -> {
            var requestBuilder = Dto.FirstTitleRequest.newBuilder();
            requestBuilder
                    .setDispatcherId(shift.getDispatcherId().toString())
                    .setCommunicationType(shift.getCommunicationType())
                    .setTransportationType(shift.getTransportationType())
                    .setStartDate(shift.getStartDate().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                    .setFinishDate(shift.getFinishDate().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                    .setTariffDepartmentId(shift.getTariffDepartmentId().toString())
                    .setTransportId(shift.getTransportId().toString())
                    .setDriverEmployeeId(shift.getDriverId().toString())
                    .setId(shift.getId().toString())
                    .build();
            requestsListBuilder.addRequests(requestBuilder.build());
        });
        var result = stub.generateFirstTitle(requestsListBuilder.build());
        return result.getResponsesList().stream().map(grpcResponse -> {
            var response = new FirstTitleResponseDto();
            if(grpcResponse.getErrorText().isEmpty()){
                response.setEwbId(UUID.fromString(grpcResponse.getEwbId()));
                response.setContent(grpcResponse.getContent().toByteArray());
                response.setCreationTime(LocalDateTime.ofEpochSecond(
                        grpcResponse.getCreationTime().getSeconds(),
                        grpcResponse.getCreationTime().getNanos(),
                        ZoneOffset.UTC));
            }
            response.setHumanReadableId(grpcResponse.getHumanReadableId());
            response.setFileName(grpcResponse.getFileName());
            response.setErrorText(grpcResponse.getErrorText());
            response.setShiftId(UUID.fromString(grpcResponse.getId()));
            return response;
        }).toList();
    }
}
