package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.ContractMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingContract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class CarsharingContractMapperTest {
    
    private final CarsharingContractMapper mapper = Mappers.getMapper(CarsharingContractMapper.class);
    
    @Test
    void messageToEntity() {
        ContractMessage message = ContractMessage.builder()
                                                 .id(UUID.randomUUID())
                                                 .serviceType("EMPLOYEE_TRANSPORTATION")
                                                 .transportType(TransportTypeEnum.CARSHARING.getName())
                                                 .region("Region")
                                                 .contractorId(UUID.randomUUID())
                                                 .sum(1000L)
                                                 .startDate(LocalDate.of(2021, 4, 20))
                                                 .endDate(LocalDate.of(2021, 4, 21))
                                                 .userId(UUID.randomUUID())
                                                 .creationTime(LocalDateTime.now())
                                                 .organizations(Set.of(UUID.randomUUID(), UUID.randomUUID()))
                                                 .build();
        CarsharingContract contract = mapper.messageToEntity(message);
        
        assertThat(contract).isNotNull();
        assertThat(contract.getId()).isEqualTo(message.getId());
        assertThat(contract.getServiceType().name()).isEqualTo(message.getServiceType());
        assertThat(contract.getRegion()).isEqualTo(message.getRegion());
        assertThat(contract.getTransportType().name()).isEqualTo(message.getTransportType());
        assertThat(contract.getContractorId()).isEqualTo(message.getContractorId());
        assertThat(contract.getOrganizations().size()).isEqualTo(message.getOrganizations().size());
        assertThat(contract.isActive()).isEqualTo(message.isActive());
        assertThat(contract.isDeleted()).isEqualTo(message.isDeleted());
    }
}