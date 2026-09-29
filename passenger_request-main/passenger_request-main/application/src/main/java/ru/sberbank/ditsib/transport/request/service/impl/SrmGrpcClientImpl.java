package ru.sberbank.ditsib.transport.request.service.impl;


import com.google.protobuf.NullValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.srm.grpc.dto.SrmDescriptor;
import ru.sber.transport.srm.grpc.service.SrmServiceGrpc;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.dto.CancelSharedRideDTO;
import ru.sberbank.ditsib.transport.request.mappers.SrmMapper;
import ru.sberbank.ditsib.transport.request.service.SrmGrpcClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.UUID;

/**
 * gRPC-клиент SRM.
 */
@Component
@RequiredArgsConstructor
class SrmGrpcClientImpl implements SrmGrpcClient {
    
    private final SrmMapper mapper;
    
    @GrpcClient("srm")
    private SrmServiceGrpc.SrmServiceBlockingStub stub;
    
    @Override
    public CancelSharedRideDTO cancelRequestGrpc(UUID requestId) {
        var response = stub.cancelRequest(createCancelRequest(requestId));
        return decodeCancelResponse(response);
    }
    
    @Override
    public SrmSharedRideDTO getSharedRideGrpc(@NonNull UUID sharedRideId) {
        var response = stub.getRequest(createGetRequest(sharedRideId));
        return mapper.mapToDTO(response);
    }
    
    private SrmDescriptor.SrmCancelRequest createCancelRequest(UUID requestId) {
        return SrmDescriptor.SrmCancelRequest.newBuilder()
                                             .setRequestId(requestId.toString())
                                             .build();
    }
    
    private CancelSharedRideDTO decodeCancelResponse(Iterator<SrmDescriptor.SrmCancelResponse> response) {
        if (response.hasNext()) {
            var srmCancelResponse = response.next();
            
            SrmDescriptor.NullableString ownerRequest = srmCancelResponse.getOwnerRequest();
            UUID ownerRequestId = null;
            if (ownerRequest.hasData()) {
                ownerRequestId = UUID.fromString(ownerRequest.getData());
            }
            return CancelSharedRideDTO.builder()
                                      .result(srmCancelResponse.getResult())
                                      .ownerRequest(ownerRequestId)
                                      .errorDescription(srmCancelResponse.getErrorDescription())
                                      .build();
        } else {
            return null;
        }
    }
    
    private SrmDescriptor.SrmGetRequest createGetRequest(@NonNull UUID sharedRideId) {
        return SrmDescriptor.SrmGetRequest.newBuilder()
                                          .setSharedRideId(sharedRideId.toString())
                                          .build();
    }
    
    private LocalDateTime convertGoogleTimestampToLocalDateTime(com.google.protobuf.Timestamp timestamp) {
        return Instant
                .ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
    
    private ZonedDateTime convertGoogleTimestampToZonedDateTime(com.google.protobuf.Timestamp timestamp) {
        LocalDateTime ldt = convertGoogleTimestampToLocalDateTime(timestamp);
        return ldt.atZone(ZoneOffset.UTC);
    }
    
    private Double getNullableValue(SrmDescriptor.NullableDouble source) {
        if (source.getNull().equals(NullValue.NULL_VALUE)) {
            return null;
        } else {
            return source.getData();
        }
    }
}

