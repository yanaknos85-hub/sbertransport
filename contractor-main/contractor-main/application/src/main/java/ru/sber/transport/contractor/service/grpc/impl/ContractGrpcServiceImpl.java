package ru.sber.transport.contractor.service.grpc.impl;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.contractor.exceptions.business.HaveActiveContractsException;
import ru.sber.transport.contractor.service.grpc.ContractGrpcService;
import ru.sberbank.ditsib.transport.tariff_fleet.grpc.dto.HasActiveByContractorIdRequest;
import ru.sberbank.ditsib.transport.tariff_fleet.grpc.service.ContractsServiceGrpc;

import java.util.UUID;

import static ru.sber.transport.contractor.exceptions.business.HaveActiveContractsException.DEACTIVATE_CONTRACTOR_WITH_ACTIVE_CONTRACTS_MSG;

@Slf4j
@Service
public class ContractGrpcServiceImpl implements ContractGrpcService {

    @GrpcClient("grpc-tariff-fleet")
    private ContractsServiceGrpc.ContractsServiceBlockingStub contractsServiceBlockingStub;

    @Override
    public void haveActiveContractsByContractorId(@NotNull UUID contractorId) {
        boolean isAnyActiveContracts;
        try {
            isAnyActiveContracts = contractsServiceBlockingStub.haveActiveContractsByContractorId(HasActiveByContractorIdRequest.newBuilder()
                    .setContractorId(contractorId.toString())
                    .build()).getValue();
        } catch (Exception e) {
            log.error("Error while checking active autoservice contracts by contractor id: {}", contractorId, e);
            throw e;
        }

        if (isAnyActiveContracts) {
            throw new HaveActiveContractsException(DEACTIVATE_CONTRACTOR_WITH_ACTIVE_CONTRACTS_MSG);
        }
    }
}
