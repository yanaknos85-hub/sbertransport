package ru.sber.transport.contractor.service.grpc;

import java.util.UUID;

public interface ContractGrpcService {

    void haveActiveContractsByContractorId(UUID contractorId);
}
