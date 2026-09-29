package ru.sberbank.ditsib.transport.vehicle.service.grpc;

import io.grpc.stub.StreamObserver;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.transport.grpc.dto.Dto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;
import ru.sberbank.ditsib.transport.vehicle.exception.AttorneyNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.service.AttorneyService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleGrpcServiceTest {
    
    @Mock
    private StreamObserver<Dto.AttorneyInfoResponse> responseObserverAttorneyInfo;
    
    @Mock
    private AttorneyService attorneyService;
    @InjectMocks
    private VehicleGrpcService vehicleGrpcService;
    
    
    @Test
    void getAttorneyInfo() {
        var request = Dto.AttorneyInfoRequest.newBuilder()
                                             .setTelemechanicId(UUID.randomUUID().toString())
                                             .build();
        doReturn(Instancio.create(AttorneyDto.class)).when(attorneyService).getAttorneyByTelemechanicId(any(UUID.class));
        doNothing().when(responseObserverAttorneyInfo).onNext(any(Dto.AttorneyInfoResponse.class));
        doNothing().when(responseObserverAttorneyInfo).onCompleted();
        vehicleGrpcService.getAttorneyInfo(request, responseObserverAttorneyInfo);
        verify(responseObserverAttorneyInfo, times(1)).onNext(any(Dto.AttorneyInfoResponse.class));
        verify(responseObserverAttorneyInfo, times(1)).onCompleted();
        
        doThrow(AttorneyNotFoundException.class).when(attorneyService).getAttorneyByTelemechanicId(any(UUID.class));
        doNothing().when(responseObserverAttorneyInfo).onError(any(Throwable.class));
        vehicleGrpcService.getAttorneyInfo(request, responseObserverAttorneyInfo);
        verify(responseObserverAttorneyInfo, times(1)).onError(any(Throwable.class));
    }
}