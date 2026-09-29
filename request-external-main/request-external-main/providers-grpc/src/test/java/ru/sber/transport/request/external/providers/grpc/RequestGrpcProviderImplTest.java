package ru.sber.transport.request.external.providers.grpc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.payout.grpc.dto.RequestDTO;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.State;

@ExtendWith(MockitoExtension.class)
class RequestGrpcProviderImplTest {

    @Mock
    private TripOrdersService tripOrdersService;

    @Mock
    private TripOrdersProvider tripOrdersProvider;

    @Mock
    private StreamObserver<Empty> responseObserver;

    @InjectMocks
    private RequestGrpcProviderImpl requestGrpcProvider;

    @Test
    void requestChangeStatus() {
        var requestId1 = UUID.randomUUID();
        var requestId2 = UUID.randomUUID();
        var request = RequestDTO.ChangeStatus.newBuilder()
            .addAllRequestIds(List.of(requestId1.toString(), requestId2.toString()))
            .setNewStatus(State.PAYMENT_AWAITING.name())
            .build();
        var request1 = Instancio.of(TestOrderData.class)
            .set(Select.field(TestOrderData::getId), requestId1)
            .create();

        doReturn(Optional.of(request1)).when(tripOrdersProvider).get(null, requestId1);
        doReturn(Optional.empty()).when(tripOrdersProvider).get(null, requestId2);

        requestGrpcProvider.requestChangeStatus(request, responseObserver);

        verify(tripOrdersService).edit(nullable(UUID.class), anyBoolean(), nullable(UUID.class), eq(requestId1),
            any(EditTripOrderData.class), eq(Set.of("=status")));
        verify(responseObserver).onNext(Empty.getDefaultInstance());
        verify(responseObserver).onCompleted();
    }

    @Test
    void requestChangeStatus_throws_exception() {
        var requestId1 = "wrong_uuid";
        var request = RequestDTO.ChangeStatus.newBuilder()
            .addAllRequestIds(List.of(requestId1))
            .setNewStatus(State.PAYMENT_AWAITING.name())
            .build();

        requestGrpcProvider.requestChangeStatus(request, responseObserver);

        verify(responseObserver).onError(any(Throwable.class));
    }

}