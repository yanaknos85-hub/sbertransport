package ru.sberbank.ditsib.transport.vehicle.service.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.transport.grpc.dto.Dto;
import ru.sber.transport.transport.grpc.service.VehicleServiceGrpc;
import ru.sberbank.ditsib.transport.vehicle.service.AttorneyService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class VehicleGrpcService extends VehicleServiceGrpc.VehicleServiceImplBase {
    
    private final AttorneyService attorneyService;
    
    @Override
    public void getAttorneyInfo(Dto.AttorneyInfoRequest request, StreamObserver<Dto.AttorneyInfoResponse> responseObserver) {
        try {
            var telemechanicId = UUID.fromString(request.getTelemechanicId());
            var attorney = attorneyService.getAttorneyByTelemechanicId(telemechanicId);
            responseObserver.onNext(Dto.AttorneyInfoResponse.newBuilder()
                                                            .setAttorneyId(attorney.attorneyId().toString())
                                                            .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getAttorneyInfo: %s".formatted(e.getMessage()));
            responseObserver.onError(e);
        }
    }
    
}
