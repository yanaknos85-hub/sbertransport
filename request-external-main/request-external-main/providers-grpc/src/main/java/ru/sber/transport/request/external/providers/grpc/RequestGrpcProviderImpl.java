package ru.sber.transport.request.external.providers.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.payout.grpc.dto.RequestDTO;
import ru.sber.transport.payout.grpc.service.RequestServiceGrpc;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.business.impl.ChangeStateRequest;
import ru.sber.transport.request.external.model.State;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RequestGrpcProviderImpl extends RequestServiceGrpc.RequestServiceImplBase {

    private final TripOrdersService tripOrdersService;

    private final TripOrdersProvider tripOrdersProvider;

    @Override
    public void requestChangeStatus(RequestDTO.ChangeStatus request, StreamObserver<Empty> responseObserver) {
        try {
            var status = State.valueOf(request.getNewStatus());
            for (var requestId : request.getRequestIdsList()) {
                var id = UUID.fromString(requestId);
                var optionalRequest = tripOrdersProvider.get(null, id);
                if (optionalRequest.isEmpty()) {
                    log.warn("Trip order with id {} not found", id);
                    continue;
                }
                tripOrdersService.edit(null, false, null, id, new ChangeStateRequest(optionalRequest.get(), status),
                    Set.of("=status"));
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

}
