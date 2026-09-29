package ru.sber.transport.contractor.service.impl.grpc;

import com.google.protobuf.BoolValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.contractor.exceptions.business.HaveActiveContractsException;
import ru.sber.transport.contractor.service.grpc.impl.ContractGrpcServiceImpl;
import ru.sberbank.ditsib.transport.tariff_fleet.grpc.dto.HasActiveByContractorIdRequest;
import ru.sberbank.ditsib.transport.tariff_fleet.grpc.service.ContractsServiceGrpc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractGrpcServiceImplTest {

    @InjectMocks
    private ContractGrpcServiceImpl contractGrpcService;
    @Mock
    private ContractsServiceGrpc.ContractsServiceBlockingStub contractsServiceBlockingStub;

    @Test
    void haveActiveContractsByContractorId() {
        var contractorId = UUID.randomUUID();
        var request = HasActiveByContractorIdRequest.newBuilder().setContractorId(contractorId.toString()).build();
        doReturn(BoolValue.of(true)).when(contractsServiceBlockingStub).haveActiveContractsByContractorId(request);
        assertThrows(HaveActiveContractsException.class,
                () -> contractGrpcService.haveActiveContractsByContractorId(contractorId));
        doReturn(BoolValue.of(false)).when(contractsServiceBlockingStub).haveActiveContractsByContractorId(request);
        contractGrpcService.haveActiveContractsByContractorId(contractorId);
        verify(contractsServiceBlockingStub, times(2)).haveActiveContractsByContractorId(request);
    }
}
