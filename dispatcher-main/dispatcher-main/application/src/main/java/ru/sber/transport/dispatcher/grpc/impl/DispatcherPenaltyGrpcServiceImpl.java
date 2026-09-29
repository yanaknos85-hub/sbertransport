package ru.sber.transport.dispatcher.grpc.impl;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.grpc.dto.GetOrganizationsForPenaltiesRequest;
import ru.sber.transport.dispatcher.grpc.dto.GetOrganizationsForPenaltiesResponse;
import ru.sber.transport.dispatcher.grpc.service.DispatcherPenaltyServiceGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DispatcherPenaltyGrpcServiceImpl extends DispatcherPenaltyServiceGrpc.DispatcherPenaltyServiceImplBase {

    private final ContractorRepository contractorRepository;

    @Override
    public void getOrganizationsForPenalties(GetOrganizationsForPenaltiesRequest request,
                                             StreamObserver<GetOrganizationsForPenaltiesResponse> responseObserver) {
        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();
        var response = GetOrganizationsForPenaltiesResponse.newBuilder()
                .addAllTins(tins)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
