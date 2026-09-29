package ru.sber.transport.telemechanic.service.grpc.impl;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;
import ru.sber.transport.telemechanic.config.properties.EwbGrpcFirstTitleProperties;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sber.transport.telemechanic.service.EwbService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class EwbGrpcServiceImpl extends EwbServiceGrpc.EwbServiceImplBase {

    private final EwbService ewbService;
    private final DriverService driverService;
    private final Clock clock;
    private final EwbGrpcFirstTitleProperties ewbGrpcFirstTitleProperties;
    
    @Override
    public void haveActiveEwb(Dto.HaveActiveEwbDto request, StreamObserver<BoolValue> responseObserver) {
        try {
            var departmentIds = request.getDepartmentIdsList().stream()
                                       .map(UUID::fromString)
                                       .toList();
            var checkStartDate = LocalDate.from(Instant.ofEpochSecond(request.getCheckStartDate().getSeconds(),
                                                                      request.getCheckStartDate().getNanos()).atOffset(ZoneOffset.of(clock.getZone().getId())));
            responseObserver.onNext(BoolValue.of(ewbService.haveActiveEwb(departmentIds, checkStartDate)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in haveActiveEwb: %s".formatted(e.getMessage()));
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void generateFirstTitle(Dto.FirstTitleRequestList request, StreamObserver<Dto.FirstTitleResponseList> responseObserver) {
        try {
            var maxRequestSize = ewbGrpcFirstTitleProperties.maxRequestSize();
            var requestsCount = request.getRequestsCount();
            
            if (requestsCount > maxRequestSize) {
                var errorMessage = "Превышен лимит запросов: допустимо не более %d запросов, получено %d".formatted(maxRequestSize, requestsCount);
                log.error("Error in generateFirstTitle: {}", errorMessage);
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(errorMessage)));
                return;
            }
            
            log.info("Start parse generateFirstTitle request, requests count:{}", requestsCount);
            var responseBuilder = Dto.FirstTitleResponseList.newBuilder();
            for (int i = 0; i < requestsCount; i++) {
                var grpcRequest = request.getRequests(i);
                log.debug("Start generate first title, request:{}", grpcRequest);
                var grpcResponse = processSingleRequest(grpcRequest);
                responseBuilder.addResponses(grpcResponse);
                log.debug("Finish generate first title");
            }
            var response = responseBuilder.build();
            responseObserver.onNext(response);
            log.info("Finish parse generateFirstTitle request, responses count:{}", response.getResponsesCount());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in generateFirstTitle: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
    
    private Dto.FirstTitleResponse processSingleRequest(Dto.FirstTitleRequest grpcRequest) {
        try {
            var userId = UUID.fromString(grpcRequest.getDispatcherId());
            var ewbUuid = ewbService.getUUID();
            var driver = driverService.getByEmployeeId(UUID.fromString(grpcRequest.getDriverEmployeeId()));
            var firstTitleRequest = new FirstTitleRequest(
                    ewbUuid.uuid(),
                    LocalDate.parse(grpcRequest.getStartDate()),
                    LocalDate.parse(grpcRequest.getFinishDate()),
                    grpcRequest.getTransportationType(),
                    grpcRequest.getCommunicationType(),
                    UUID.fromString(grpcRequest.getTariffDepartmentId()),
                    UUID.fromString(grpcRequest.getTransportId()),
                    driver.getId()
            );
            var response = ewbService.generateFirstTitle(firstTitleRequest, userId);
            var responseBuilder = Dto.FirstTitleResponse.newBuilder()
                    .setId(grpcRequest.getId())
                    .setEwbId(ewbUuid.uuid().toString())
                    .setHumanReadableId(response.humanReadableId())
                    .setFileName(response.fileName())
                    .setContent(ByteString.copyFrom(response.content()))
                    .setCreationTime(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(response.creationTime().toInstant(ZoneOffset.UTC).getEpochSecond())
                            .setNanos(response.creationTime().toInstant(ZoneOffset.UTC).getNano())
                            .build());
            return responseBuilder.build();
        } catch (Exception e) {
            log.error("Error in generateFirstTitle, request:{}, error:{}", grpcRequest, e.getMessage(), e);
            return Dto.FirstTitleResponse.newBuilder()
                    .setId(grpcRequest.getId())
                    .setErrorText(e.getMessage())
                    .build();
        }
    }
}