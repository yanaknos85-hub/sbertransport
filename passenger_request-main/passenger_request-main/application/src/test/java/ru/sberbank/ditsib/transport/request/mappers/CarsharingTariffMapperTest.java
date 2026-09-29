package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.TariffMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class CarsharingTariffMapperTest {
    
    private final CarsharingTariffMapper mapper = Mappers.getMapper(CarsharingTariffMapper.class);
    
    @Test
    void messageToEntity() {
        TariffMessage message = TariffMessage.builder()
                                            .id(UUID.randomUUID())
                                            .humanReadableId("TF-0001-01")
                                            .region("Region")
                                            .transportTypeId(TransportTypeEnum.CARSHARING.getId())
                                            .contractId(UUID.randomUUID())
                                            .deleted(false)
                                            .build();
        
        CarsharingTariff tariff = mapper.messageToEntity(message);
        
        assertThat(tariff).isNotNull();
        assertThat(tariff.getId()).isEqualTo(message.getId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
        assertThat(tariff.getRegion()).isEqualTo(message.getRegion());
        assertThat(tariff.getTransportType().getId()).isEqualTo(message.getTransportTypeId());
        assertThat(tariff.getContractId()).isEqualTo(message.getContractId());
    }
}